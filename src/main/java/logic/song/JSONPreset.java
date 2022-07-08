package logic.song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JSONPreset {
	private String name;
	private String[] jsonPathKeys;
	private String baseJSONPath;
	
	private static ArrayList<JSONPreset> presets = new ArrayList<>();
	
	static {
		List<String> defaultList = List.of(new String[] {"name", "artists", "albumURL",
				"albumURI", "trackNumber"});
		
		// Search preset
		List<String> searchList = new ArrayList<>(defaultList);
		searchList.add("previewURL");
		String[] searchArray = searchList.toArray(new String[searchList.size()]);
		new JSONPreset("search", "$.tracks.items", searchArray);
		
		// PlaybackState preset
		List<String> playbackList = new ArrayList<>(defaultList);
		String[] playbackArray = {"duration", "progress", "timestamp", 
				"playing", "ID"};
		Collections.addAll(playbackList, playbackArray);
		
		playbackArray = playbackList.toArray(new String[playbackList.size()]);
		new JSONPreset("playbackState", "$.item", playbackArray);
		
		// CurrentSong preset
		String[] currentSongArray = defaultList.toArray(new String[defaultList.size()]);
		new JSONPreset("currentSong", "$.item", currentSongArray);
		
		// RecentlyPlayed preset
		Map<String, String> previousJsonPaths = Song.getJsonPathsMap();
		
//		Map<String, String> jsonPaths = new HashMap<>();
//		jsonPaths.put("name", ".track.name");
//		jsonPaths.put("artists", ".track.artists[*]");
//		jsonPaths.put("albumURL", ".track.album.images[0].url");
//		jsonPaths.put("previewURL", ".track.preview_url");
//		jsonPaths.put("contextURI", ".context.uri");
//		jsonPaths.put("duration", ".track.duration_ms");
//		setJsonPaths(jsonPaths);
//		
//		// initializes Song object(s) from the specified field paths in the returned JSON
//		String[] extendedFields = {"duration"};
//		allSongValues = mapValuesFromJSON(returnJson, "$.items", extendedFields);
//		
//		setJsonPaths(previousJsonPaths);
		
	}
	
	public JSONPreset() {
		presets.add(this);
	}
	
	public JSONPreset(String name, String baseJSONPath, String... jsonPathKeys) {
		this();
		setName(name);
		setBaseJSONPath(baseJSONPath);
		setJsonPathKeys(jsonPathKeys);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getJsonPathKeys() {
		return getDeepCopy(this.jsonPathKeys);
	}

	public void setJsonPathKeys(String... jsonPathKeys) {
		this.jsonPathKeys = getDeepCopy(jsonPathKeys);
	}

	public String getBaseJSONPath() {
		return baseJSONPath;
	}

	public void setBaseJSONPath(String baseJSONPath) {
		this.baseJSONPath = baseJSONPath;
	}
	
	private String[] getDeepCopy(String[] array) {
		String[] deepCopy =  new String[array.length];
		for (int i = 0; i < deepCopy.length; i++) {
			deepCopy[i] = array[i];
		}
		return deepCopy;
	}
	
	public static JSONPreset getJSONPresetbyName(String name) {
		for (JSONPreset preset : presets) {
			if (preset.getName().equalsIgnoreCase(name)) {
				return preset;
			}
		}
		
		return null;
	}
}
