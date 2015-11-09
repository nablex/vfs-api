package be.nabu.libs.vfs.api.features;

/**
 * You can set a reason on this object. The reason is likely linked to why you want to manipulate it.
 */
public interface Reasoned {
	public void setReason(String reason);
	public String getReason();
}
