/*
* Copyright (C) 2015 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.libs.vfs.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Date;

public interface File extends Iterable<File> {

	/**
	 * Check if the file exists
	 */
	public boolean exists();

	/**
	 * Indicates whether or not this object contains other file objects 
	 */
	public boolean isDirectory();
	
	/**
	 * Indicates whether or not this object contains any data
	 */
	public boolean isFile();

	/**
	 * Check if the file is readable
	 */
	public boolean isReadable();
	
	/**
	 * Check if the file is writable
	 */
	public boolean isWritable();

	/**
	 * Resolves the given path against this file object. It allows you to resolve a relative path without having to concatenate or resorting to the file system in the backend.
	 * Note that this file object likely has a principal governing it as dictated by the file system resolution
	 * If that is the case, said principal is inherited by the file that is returned
	 * @param path A path relative to this file or absolute to the file system
	 */
	public File resolve(String path) throws IOException;
	
	/**
	 * Returns the file system that is used by this file
	 */
	public FileSystem getFileSystem();
	
	/**
	 * Returns the parent file. Must return "null" if this file object is the root
	 * @return Null if the object is the root or if the object does not exist. Differentiate with exists()
	 */
	public File getParent() throws IOException;
	
	/**
	 * Returns the absolute path starting from the file system to this file
	 * The root should return "/"
	 */
	public String getPath();
	
	/**
	 * Returns the name of this file. Must return "null" if it is the root
	 * @return Null if the object is the root or if the object does not exist. Differentiate with exists()
	 */
	public String getName();
	
	/**
	 * Returns the size of this object (only applicable if it has content)
	 * Returns -1 if the size could not be determined
	 */
	public long getSize() throws IOException;
	
	/**
	 * Returns the content type of this file. Note that directories also have a content type
	 */
	public String getContentType() throws IOException;
	
	/**
	 * Returns the last modified date for this object (only applicable if it has content)
	 * Returns null if the last modified date could not be determined
	 */
	public Date getLastModified() throws IOException;
	
	/**
	 * Returns an inputstream to the object (only applicable if it has content)
	 */
	public InputStream getInputStream() throws IOException;
	
	/**
	 * Returns an outputstream to the object (only applicable if it has content)
	 * Note that this will attempt to create the file object if it does not yet exist
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * This can only be done if the object does not yet exist
	 * It forces the system in the backend to explicitly create this file as a directory and also any missing dirs up to this file 
	 */
	public void mkdir() throws IOException;
	
	/**
	 * Deletes the current file
	 */
	public void delete() throws IOException;
	
	/**
	 * Allows the implementation to optimize a move depending on environmental factors
	 * Note that the principal is for this file (if any) is not inherited by the target file because it may be in another filesystem altogether
	 */
	public void move(File target) throws IOException;

	/**
	 * Clone this object for a particular principal
	 * Note that this method may throw a SecurityException if the new principal does not have access to it 
	 */
	public File cloneFor(Principal principal);
}
