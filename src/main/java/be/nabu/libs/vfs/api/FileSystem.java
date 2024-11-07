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

import java.io.Closeable;
import java.io.IOException;
import java.security.Principal;

import be.nabu.libs.events.api.EventDispatcher;

/**
 * The filesystem allows you to find files. How it does this and what security applies, which transformations (if any) take place etc, is all dependent on the implementation
 * The close() must release any resources that the filesystem has opened
 */
public interface FileSystem extends Closeable {
	/**
	 * Resolve the file against the root of this filesystem
	 * You can use an absolute path or a relative path but the relative path is still evaluated against the root so implicitly absolute
	 * The principal can determine which transformations (if any) apply, which view the user sees (e.g. "/home/<username>") etc.
	 */
	public File resolve(String path, Principal principal) throws IOException;
	
	/**
	 * Because in a lot of scenarios the principal will not be used, this convenience method was added.
	 * This is equivalent to calling resolve(path, null);
	 * This will be implemented as a default once we hit java 8
	 */
	public File resolve(String path) throws IOException;

	/**
	 * The event dispatcher for this file system, allowing you to monitor changes
	 */
	public EventDispatcher getEventDispatcher();
}
