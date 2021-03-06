package com.mesosphere.sdk.storage;

import java.util.Collection;
import java.util.Map;

/**
 * A low-level interface for key/value storage in a tree structure.
 * <p>
 * <p>Individual nodes may be the parent of other nodes. Some of these parent nodes may lack any data of their own. The
 * root-level node (with path "", or "/") is considered to always be present.
 * <p>
 * <p>This interface should be implemented in order to store and fetch data, with paths delimited by
 * {@link com.mesosphere.sdk.storage.PersisterUtils#PATH_DELIM}.
 */
public interface Persister {

  /**
   * Retrieves the previously stored data at the specified path, or throws an exception if the path doesn't exist.
   * If the path is valid but has no data (i.e. is a parent of another path), this returns {@code null}.
   *
   * @throws PersisterException if the requested path doesn't exist, or for other access errors
   */
  byte[] get(String path) throws PersisterException;

  /**
   * Returns the names of child nodes at the provided path. Some returned nodes may have {@code null} data when
   * retrieved via {@link #get(String)}, indicating that these are stub parent entries to other nodes.
   * <p>
   * <p>To translate the returned values to full absolute paths, they may be joined with the provided {@code path}
   * input using {@link PersisterUtils#join(String, String)}.
   * <p>
   * <p>To get the list of nodes at the root, use "" or "/" as the input path.
   *
   * @throws PersisterException if the requested path doesn't exist, or for other access errors
   */
  Collection<String> getChildren(String path) throws PersisterException;

  /**
   * Writes a single value to storage at the specified path, replacing any existing data at the path or creating the
   * path if it doesn't exist yet.
   *
   * @throws PersisterException in the event of an access error
   */
  void set(String path, byte[] bytes) throws PersisterException;

  /**
   * Atomically reads many values from storage at once, returning a mapping of paths to values. Values which are
   * missing will be set to {@code null} in the returned {@link Map}.
   *
   * @throws PersisterException in the event of an access error
   * @see #get(String)
   */
  Map<String, byte[]> getMany(Collection<String> paths) throws PersisterException;

  /**
   * Atomically writes many values to storage at once.
   *
   * @throws PersisterException in the event of an access error, in which case no changes should have been made
   * @see #set(String, byte[])
   */
  void setMany(Map<String, byte[]> pathBytesMap) throws PersisterException;

  /**
   * Recursively copies the node and its contents. This operation has two steps:
   * 1. Walk all the nodes gathering data.
   * 2. Commit the transaction to write new nodes.
   * After (1) is done and before the completion of (2) there may be updates to nodes.
   * Currently, this is used only for migrating the data which occurs during bootstrap and there should not be any
   * conflicts. Be careful when using this method for other runtime operations.
   *
   * @param srcPath  The source path of the node to copy from.
   * @param destPath The destination path of the node to copy to.
   * @throws PersisterException if the source node is not present or the destination node is already present
   */
  void recursiveCopy(String srcPath, String destPath) throws PersisterException;

  /**
   * Atomically deletes many values from storage at once, with each path entry being treated as a call to
   * {@link #recursiveDelete(String)} for those paths.
   *
   * @throws PersisterException in the event of an access error, in which case no changes should have been made
   * @see #recursiveDelete(String)
   */
  void recursiveDeleteMany(Collection<String> paths) throws PersisterException;

  /**
   * Recursively deletes the data at the specified path, or throws an exception if no data existed at that location.
   * <p>
   * <p>Deleting the root node (as "" or "/") will result in all nodes EXCEPT the root node being deleted.
   *
   * @throws PersisterException if the data at the requested path didn't exist, or for other access errors
   */
  void recursiveDelete(String path) throws PersisterException;

  /**
   * Closes this storage and cleans up any local client resources. No other operations should be performed against the
   * instance after calling this.
   */
  void close();
}
