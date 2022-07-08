package logic.device;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;

import logic.song.Song;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class Device {
	private String id;
	private boolean active;
	private String name;
	private boolean privateSession;
	
	public Device() {}
	
	public Device(String id, boolean active, String name, boolean privateSession) {
		this.id = id;
		this.active = active;
		this.name = name;
		this.privateSession = privateSession;
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
	public boolean isPrivateSession() {
		return privateSession;
	}
	public void setPrivateSession(boolean privateSession) {
		this.privateSession = privateSession;
	}
	
	public void set (String field, Object value) {
		switch (field) {
		case "id": 
			this.setId(String.valueOf(value));
			break;
		case "active": 
			this.setActive((boolean) value);
			break;
		case "name": 
			this.setName(String.valueOf(value));
			break;
		case "privateSession": 
			this.setPrivateSession((boolean) value); 
			break;
		}
	}
	
	public static Device[] initializeFromJSON(String json) {
		
		Device[] devices = new Device[0];
		
		String jsonPath = (json.contains("devices")) ? "$.devices" : "$.device";
		
		// Returns a JSONArray with LinkedHashMaps or a LinkedHashMap
		Object devicesObj = JsonPath.parse(json).read(jsonPath);
		
		int devicesCount = (jsonPath.equals("$.devices")) ? 
				((JSONArray) devicesObj).size() : 1;
		
		devices = new Device[devicesCount];
		for (int d = 0; d < devices.length; d++) {
			devices[d] = new Device();
		}
		
		int i = 0;
		do {
			LinkedHashMap<String, Object> device = null;
			
			if (devicesObj instanceof LinkedHashMap) {
				device = (LinkedHashMap<String, Object>) devicesObj;
			} else if (devicesObj instanceof JSONArray) {
				device = (LinkedHashMap<String, Object>) ((JSONArray) devicesObj).get(i);
			}
			
			devices[i].set("id", device.get("id"));
			devices[i].set("active", device.get("is_active"));
			devices[i].set("name", device.get("name"));
			devices[i].set("privateSession", device.get("is_private_session"));
			i++;
		} while (i < devicesCount);
		
		return devices;
	}
	
	public static Device initializeThisDevice(String json) {
		
		Device[] devices = initializeFromJSON(json);
		Device thisDevice = getThisDevice(devices);
		
		return thisDevice;
	}
	
	public static Device getThisDevice(Device[] devices) {
		
		Device thisDevice = null;
		
		try {
			// Gets computer name, which is the name of this Spotify device
			String hostname = "Unknown";
		    InetAddress addr = InetAddress.getLocalHost();
		    hostname = addr.getHostName();
		    
		    for (Device d : devices) {
		    	
		    	if (d.getName().equals(hostname)) {
					thisDevice = d;
				}
		    }
			
		} catch (UnknownHostException ex) {
		    System.out.println("Hostname can not be resolved");
		}
		
		return thisDevice;
	}
}
