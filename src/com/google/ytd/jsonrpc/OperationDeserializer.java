package com.google.ytd.jsonrpc;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class OperationDeserializer implements JsonDeserializer<Map<String, String>> {
	@Override
	public Map<String, String> deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		Set<Entry<String, JsonElement>> entries = json.getAsJsonObject().entrySet();

		Map<String, String> map = new HashMap<String, String>();

		Iterator<Entry<String, JsonElement>> iterator = entries.iterator();

		while (iterator.hasNext()) {
			Entry<String, JsonElement> entry = iterator.next();
			String key = entry.getKey();
			JsonElement value = entry.getValue();
			map.put(key, value.getAsString());
		}

		return map;
	}
}