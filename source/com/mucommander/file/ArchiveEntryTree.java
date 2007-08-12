/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Stores archive entries and organizes them in a tree structure that maps entries in the way they are organized
 * inside the archive. An instance of <code>ArchiveEntryTree</code> also acts as the root node: all archive entries are
 * descendants of it.
 *
 * @author Maxence Bernard
 */
public class ArchiveEntryTree extends DefaultMutableTreeNode {

    /**
     * Creates a new empty tree.
     */
    public ArchiveEntryTree() {
    }

    /**
     * Adds the given entry to the archive tree, creating parent nodes as necessary.
     *
     * @param entry the entry to add to the tree
     */
    public void addArchiveEntry(ArchiveEntry entry) {

        String entryPath = entry.getPath();
        int entryDepth = entry.getDepth();
        int slashPos = 0;
        DefaultMutableTreeNode node = this;
        for(int d=0; d<=entryDepth; d++) {
            if(d==entryDepth && !entry.isDirectory()) {
                // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating leaf for "+entryPath);
                node.add(new DefaultMutableTreeNode(entry, true));
                break;
            }

            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");

            int nbChildren = node.getChildCount();
            DefaultMutableTreeNode childNode = null;
            boolean matchFound = false;
            for(int c=0; c<nbChildren; c++) {
                childNode = (DefaultMutableTreeNode)node.getChildAt(c);
                if(((ArchiveEntry)childNode.getUserObject()).getPath().equals(subPath)) {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                    matchFound = true;
                    break;
                }
            }

            if(matchFound) {
                if(d==entryDepth) {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Replacing entry for node "+childNode);
                    // Replace existing entry
                    childNode.setUserObject(entry);
                }
                else {
                    node = childNode;
                }
            }
            else {
                if(d==entryDepth) {		// Leaf
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+entryPath);
                    node.add(new DefaultMutableTreeNode(entry, true));
                }
                else {
                    // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Creating node for "+subPath);
                    childNode = new DefaultMutableTreeNode(new SimpleArchiveEntry(subPath, entry.getDate(), 0, true), true);
                    node.add(childNode);
                    node = childNode;
                }
            }
        }
    }


    /**
     * Finds and returns the node that corresponds to the specified entry path, null if no entry matching the path
     * could be found.
     *
     * <p>Important note: the given path's separator character must be '/' and the path must be relative to the
     * archive's root, i.e. not start with a leading '/', otherwise the entry will not be found. Trailing separators
     * are ignored when paths are compared, for example the path 'temp' will match the entry 'temp/'.
     */
    public DefaultMutableTreeNode findEntryNode(String entryPath) {
        int entryDepth = ArchiveEntry.getDepth(entryPath);
        int slashPos = 0;
        DefaultMutableTreeNode currentNode = this;
        for(int d=0; d<=entryDepth; d++) {
            String subPath = d==entryDepth?entryPath:entryPath.substring(0, (slashPos=entryPath.indexOf('/', slashPos)+1));
            if(subPath.charAt(subPath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                subPath = subPath.substring(0, subPath.length()-1);

            // if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("subPath="+subPath+" depth="+d+"("+entryDepth+")");

            int nbChildren = currentNode.getChildCount();
            DefaultMutableTreeNode matchNode = null;
            for(int c=0; c<nbChildren; c++) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)currentNode.getChildAt(c);

                String childNodePath = ((ArchiveEntry)childNode.getUserObject()).getPath();
                if(childNodePath.charAt(childNodePath.length()-1)=='/')     // Remove any trailing slash to compare paths without trailing slashs
                    childNodePath = childNodePath.substring(0, childNodePath.length()-1);

                if(childNodePath.equals(subPath)) {
                    //					if(com.mucommander.Debug.ON) com.mucommander.Debug.trace("Found match for "+subPath);
                    matchNode = childNode;
                    break;
                }
            }

            if(matchNode==null)
                return null;    // No node maching the provided path, return null

            currentNode = matchNode;
        }

        return currentNode;
    }
}
