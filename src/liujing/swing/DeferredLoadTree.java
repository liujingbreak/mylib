package liujing.swing;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.util.*;
import java.util.logging.*;
import java.awt.event.*;
/**
Usage:
setLoader()

invoke addNodeObject() and addLeafObject() from DeferredNode

*/
public class DeferredLoadTree extends JTree{

    private DefaultMutableTreeNode rootNode;
    private EventHandler handler = new EventHandler();
    private TreeNodeLoader loader;
    private LoadModel expandingLoadModel;

    public DeferredLoadTree(String initRoot){
        rootNode = new DefaultMutableTreeNode();
        DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
        rootNode.setUserObject(initRoot);
        treeModel.setRoot(rootNode);
        addTreeWillExpandListener(handler);
        setCellRenderer(new MyTreeCellRender());
        expandingLoadModel = new LoadModel();
    }

    public void resetRootObject(Object rootUserObject){
        DefaultTreeModel treeModel = (DefaultTreeModel)getModel();
        rootNode.setUserObject(rootUserObject);
        rootNode.removeAllChildren();
        expandingLoadModel.treeNode = rootNode;
        loader.load(rootUserObject, expandingLoadModel);
        treeModel.nodeStructureChanged(rootNode);
    }

    public void setLoader(TreeNodeLoader loader){
        this.loader = loader;
    }

    private class TempUserObject{
        public String toString(){
            return "loading...";
        }
    }

    private class EventHandler implements TreeWillExpandListener, TreeExpansionListener{

        public void treeWillCollapse(TreeExpansionEvent event){}

        public void treeWillExpand(TreeExpansionEvent event){

            DefaultTreeModel treeMode = (DefaultTreeModel)getModel();
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)event.getPath().getLastPathComponent();
            if(node.getChildCount()== 1
                && ((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject() instanceof TempUserObject )
            {
                node.removeAllChildren();
                if(loader != null){
                    expandingLoadModel.treeNode = node;
                    loader.load(node.getUserObject(), expandingLoadModel);
                }
                treeMode.nodeStructureChanged(node);
            }
        }

        public void treeCollapsed(TreeExpansionEvent event){}

		public void treeExpanded(TreeExpansionEvent event){
			//try{
			//	TreePath tp = event.getPath();
			//	DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			//	if(node.getChildCount() == 1
			//		&& ( ((DefaultMutableTreeNode)node.getChildAt(0)).getUserObject() instanceof ProjectController.FolderItem))
			//	{
			//		node = (DefaultMutableTreeNode)node.getChildAt(0);
			//		TreePath path = new TreePath(node.getPath());
			//		fileTree.expandPath(path);
			//	}else if(autoExpandEnabled){
			//		TreePath path = new TreePath(node.getPath());
			//		SwingUtilities.invokeLater(new ScrollTreeViewTask(path));
			//		if(searchStarted)
			//			autoExpandEnabled = false;
			//	}
			//}catch(Exception ex){
			//	log.log(Level.SEVERE, "", ex);
			//}
		}

    }

    public class LoadModel{
        private DefaultMutableTreeNode treeNode;

        private LoadModel(){}

        public void addNodeObject(Object userObject){
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(userObject);
            treeNode.add(subNode);
            subNode.add(new DefaultMutableTreeNode(new TempUserObject()));
        }

        public void addLeafObject(Object userObject){
            DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(userObject);
            treeNode.add(subNode);
        }
    }

    public interface TreeNodeLoader{
        public void load(Object parentUserObj, LoadModel parentNode);

        public void unload(Object parentUserObj);
    }

}
