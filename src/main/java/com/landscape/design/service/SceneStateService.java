package com.landscape.design.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SceneStateService {

    private static final String DEFAULT_SCENE = "{\"version\":1,\"objects\":[]}";

    private final ObjectMapper objectMapper;

    public String emptyScene() {
        return DEFAULT_SCENE;
    }

    public int countObjects(String sceneJson) {
        if (sceneJson == null || sceneJson.isBlank()) {
            return 0;
        }
        try {
            JsonNode root = objectMapper.readTree(sceneJson);
            JsonNode objects = root.get("objects");
            if (objects == null || !objects.isArray()) {
                throw badScene();
            }
            return objects.size();
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw badScene();
        }
    }

    public void validateSceneJson(String sceneJson) {
        countObjects(sceneJson);
    }

    private static ResponseStatusException badScene() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный формат сцены (ожидается JSON с массивом objects)");
    }
}
