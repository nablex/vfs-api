package be.nabu.libs.vfs.api.events;

import be.nabu.libs.vfs.api.File;

public interface Event {
	public EventType getEventType();
	public File getFile();
	public boolean isDone();
}
