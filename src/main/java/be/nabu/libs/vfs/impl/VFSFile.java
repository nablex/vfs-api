package be.nabu.libs.vfs.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import be.nabu.libs.vfs.api.File;

/**
 * A tentative implementation that wraps a virtual file in a class compatible with java.io.File
 * Note that this is only meant to support certain legacy systems
 * There are a number of performance pitfalls (most notably FileFilter) and caveats (mostly regarding recursive searching based using list() instead of listFiles())
 *
 * It basically works by copying the virtual file to a temporary location in the actual filesystem and as such can have quite a bit of overhead
 * This is a generic file adapter but you may use more specific file adapters that have more knowledge of the actual systems in use and can optimize
 * 
 * In the future I may add a static cache with weak references. As long as the file object hasn't been gced, you could then reuse it instead of creating a new one
 * 		> this would be based on the hash/equality of the File object though so you would have to send in the same object or implement a proper equality checker
 */
public class VFSFile extends java.io.File {
	
	private File file;

	public VFSFile(File file) throws IOException {
		super(generateLocalPath());
		this.file = file;
		copyToLocal();
	}
	
	/**
	 * Dirty hack to allow for ternary overload of initial super() call in children
	 * @param file
	 * @param nullObject
	 * @throws IOException
	 */
	protected VFSFile(File file, Object nullObject) throws IOException {
		this(file);
	}
	
	protected VFSFile(File file, java.io.File localFile) {
		super(localFile.getParent(), localFile.getName());
		this.file = file;
	}
	
	protected static String generateLocalPath() {
		return System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + UUID.randomUUID().toString();
	}
	
	protected void copyToLocal() throws IOException {
		if (file.exists()) {
			// Note that the children will only be copied once they are needed
			// This means the child files will only exist if you use listFiles(), otherwise we would have to copy the entire virtual system
			if (file.isDirectory())
				super.mkdir();
			else {
				// copy the content from the vfs to the filesystem
				OutputStream output = new BufferedOutputStream(new FileOutputStream(this));
				try {
					InputStream input = new BufferedInputStream(file.getInputStream());
					try {
						int read = 0;
						byte [] buffer = new byte[10240];
						while ((read = input.read(buffer)) != -1)
							output.write(buffer, 0, read);
					}
					finally {
						input.close();
					}
				}
				finally {
					output.close();
				}
			}
		}
	}

	private static final long serialVersionUID = -2059745908496723267L;

	/**
	 * When the object is gced, the mapped file is deleted
	 * Note that if no gc occurs, it is up to the operating system to clean the temporary directory
	 */
	@Override
	protected void finalize() {
		super.delete();
	}

	@Override
	public boolean canRead() {
		return file.isReadable();
	}

	@Override
	public boolean canWrite() {
		return file.isWritable();
	}

	@Override
	public boolean createNewFile() throws IOException {
		if (!file.exists() && file.getParent() != null) {
			file.getOutputStream().close();
			return true;
		}
		return false;
	}

	@Override
	public boolean delete() {
		try {
			file.delete();
			super.delete();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public void deleteOnExit() {
		throw new RuntimeException("Seriously, don't use this");
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public String getParent() {
		try {
			return file.getParent() == null ? null : new VFSFile(file.getParent()).getAbsolutePath();
		}
		catch (IOException e) {
			return null;
		}
	}
	
	@Override
	public java.io.File getParentFile() {
		try {
			return file.getParent() == null ? null : new VFSFile(file.getParent());
		}
		catch (IOException e) {
			return null;
		}
	}

	@Override
	public boolean isDirectory() {
		return file.isDirectory();
	}

	@Override
	public boolean isFile() {
		return file.isFile();
	}

	@Override
	public boolean isHidden() {
		return file.getName().startsWith(".");
	}

	@Override
	public long lastModified() {
		try {
			return file.getLastModified().getTime();
		}
		catch (IOException e) {
			return -1;
		}
	}

	@Override
	public long length() {
		try {
			return file.getSize();
		}
		catch (IOException e) {
			return -1;
		}
	}

	@Override
	public boolean mkdir() {
		try {
			file.mkdir();
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	@Override
	public boolean mkdirs() {
		return mkdir();
	}

	@Override
	public boolean renameTo(java.io.File dest) {
		try {
			if (dest instanceof VFSFile) {
				file.move(((VFSFile) dest).file);
				return true;
			}
			else
				return super.renameTo(dest);
		}
		catch (IOException e) {
			return false;
		}
	}

	/*
	 ********************************************** List functionality
	 */
	
	@Override
	public String[] list() {
		List<java.io.File> list = list(null, null);
		String [] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			result[i] = list.get(i).getAbsolutePath();
		return result;
	}

	@Override
	public String[] list(FilenameFilter filter) {
		List<java.io.File> list = list(null, filter);
		String [] result = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			result[i] = list.get(i).getAbsolutePath();
		return result;
	}

	@Override
	public java.io.File[] listFiles() {
		List<java.io.File> list = list(null, null);
		return (java.io.File []) list.toArray(new java.io.File[list.size()]);
	}

	@Override
	public java.io.File[] listFiles(FileFilter filter) {
		List<java.io.File> list = list(filter, null);
		return (java.io.File []) list.toArray(new java.io.File[list.size()]);
	}

	@Override
	public java.io.File[] listFiles(FilenameFilter filter) {
		List<java.io.File> list = list(null, filter);
		return (java.io.File []) list.toArray(new java.io.File[list.size()]);
	}

	/**
	 * Note that using a filefilter can be _very_ expensive because the files all have to be copied to the filesystem
	 * A filenamefilter is much lighter weight
	 * @param filter1
	 * @param filter2
	 * @return
	 * @throws IOException
	 */
	private List<java.io.File> list(FileFilter filter1, FilenameFilter filter2) {
		List<java.io.File> files = new ArrayList<java.io.File>();
		try {
			for (File child : file) {
				if (filter2 != null) {
					if (filter2.accept(this, child.getName()))
						files.add(new VFSFile(child));
				}
				else if (filter1 != null) {
					VFSFile childFile = new VFSFile(child);
					if (filter1.accept(childFile))
						files.add(childFile);
					// immediately unset reference to allow gc
					else
						childFile = null;
				}
				// always add
				else
					files.add(new VFSFile(child));
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		return files;
	}
	
}
