package logic.device;

public class Device {
	private String id;
	private boolean active;
	private String name;
	
	public Device() {}
	
	public Device(String id, boolean active, String name) {
		this.id = id;
		this.active = active;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
