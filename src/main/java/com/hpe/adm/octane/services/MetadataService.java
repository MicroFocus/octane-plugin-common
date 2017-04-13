package com.hpe.adm.octane.services;

import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.Octane;
import com.hpe.adm.nga.sdk.metadata.FieldMetadata;
import com.hpe.adm.nga.sdk.network.OctaneHttpClient;
import com.hpe.adm.nga.sdk.network.OctaneHttpRequest;
import com.hpe.adm.nga.sdk.network.OctaneHttpResponse;
import com.hpe.adm.octane.services.connection.ConnectionSettings;
import com.hpe.adm.octane.services.connection.ConnectionSettingsProvider;
import com.hpe.adm.octane.services.connection.HttpClientProvider;
import com.hpe.adm.octane.services.connection.OctaneProvider;
import com.hpe.adm.octane.services.exception.ServiceRuntimeException;
import com.hpe.adm.octane.services.filtering.Entity;
import com.hpe.adm.octane.services.ui.FormLayout;
import com.hpe.adm.octane.services.util.OctaneUrlBuilder;
import com.hpe.adm.octane.services.util.Util;
import org.apache.http.client.utils.URIBuilder;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.hpe.adm.octane.services.util.Util.createQueryForMultipleValues;

public class MetadataService {

    @Inject
    protected HttpClientProvider httpClientProvider;
    @Inject
    private OctaneProvider octaneProvider;
    @Inject
    private ConnectionSettingsProvider connectionSettingsProvider;
    private Map<Entity, Collection<FieldMetadata>> cache;

    public boolean hasFields(Entity entityType, String... fieldNames) {

        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            init();
        }

        Octane octane = octaneProvider.getOctane();

        Collection<FieldMetadata> fields;

        if (!cache.containsKey(entityType)) {
            fields = octane.metadata().fields(entityType.getEntityName()).execute();
            cache.put(entityType, fields);
        } else {
            fields = cache.get(entityType);
        }

        List<String> responseFieldNames = fields.stream().map(FieldMetadata::getName).collect(Collectors.toList());

        return Arrays.stream(fieldNames)
                .allMatch(responseFieldNames::contains);
    }

    public void eagerInit(Entity... entities) {
        if (cache == null) {
            cache = new ConcurrentHashMap<>();
            init();
        }

        Octane octane = octaneProvider.getOctane();

        Arrays.stream(entities)
                .parallel()
                .forEach(entityType -> cache.put(entityType, octane.metadata().fields(entityType.getEntityName()).execute()));
    }

    private void init() {
        cache = new ConcurrentHashMap<>();
        connectionSettingsProvider.addChangeHandler(() -> cache.clear());
    }

    public List<FormLayout> getFormLayoutForAllEntityTypes() throws UnsupportedEncodingException {
        ConnectionSettings connectionSettings = connectionSettingsProvider.getConnectionSettings();
        OctaneHttpClient httpClient = httpClientProvider.geOctaneHttpClient();
        OctaneHttpResponse response = null;
        if (null == httpClient) {
            throw new ServiceRuntimeException("Failed to authenticate with current connection settings");
        }

        URIBuilder uriBuilder = OctaneUrlBuilder.buildOctaneUri(connectionSettings, "form_layouts");
        uriBuilder.setParameter("query", createQueryForMultipleValues("entity_type", Arrays.asList(
                "run", "defect", "quality_story",
                "epic", "story", "run_suite",
                "run_manual", "run_automated", "test",
                "test_automated", "test_suite", "gherkin_test",
                "test_manual", "work_item", "user_tag")));
        try {
            OctaneHttpRequest request = new OctaneHttpRequest.GetOctaneHttpRequest(uriBuilder.build().toASCIIString());
            response = httpClient.execute(request);
        } catch (Exception ex) {
            throw new ServiceRuntimeException(ex);
        }
        return Util.parseJsonWithFormLayoutData(response);
    }


}