/*
 * Copyright 2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hpe.adm.octane.ideplugins.integrationtests.services;


import com.google.inject.Inject;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.IntegrationTestBase;
import com.hpe.adm.octane.ideplugins.services.CommentService;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.MetadataService;
import com.hpe.adm.octane.ideplugins.services.connection.BasicConnectionSettingProvider;
import com.hpe.adm.octane.ideplugins.services.connection.ConnectionSettings;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.ServiceMode;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.UUID;

public class CommentServiceITCase {

    @Inject
    private CommentService commentService;

    @Inject
    private EntityService entityService;

    @Test
    public void testCommentService() {
        Collection<EntityModel> userStories = entityService.findEntities(Entity.USER_STORY);
        if (userStories.size() > 0) {
            EntityModel userStory = userStories.iterator().next();

            //Add a random comment
            String commentText = "Test comment" + UUID.randomUUID().toString();
            commentService.postComment(userStory, commentText);

            //Check if there
            Collection<EntityModel> comments = commentService.getComments(userStory);

            //Check if comment was posted
            EntityModel lastComment = comments.iterator().next();
            String lastCommentText = lastComment.getValue("text").getValue().toString();
            Assert.assertTrue(lastCommentText != null && lastCommentText.contains(commentText));

            commentService.deleteComment(lastComment.getValue("id").getValue().toString());

            comments = commentService.getComments(userStory);

            //Check if comment was deleted
            Assert.assertTrue(comments
                    .stream()
                    .map(comment -> comment.getValue("text").getValue().toString())
                    .filter(text -> text != null)
                    .noneMatch(text -> text.contains(commentText))
            );

        }
    }

    @Test
    public void fork() throws UnsupportedEncodingException {
        ServiceModule serviceModule =
                new ServiceModule(
                        new BasicConnectionSettingProvider(
                                new ConnectionSettings("https://mqalb002qngx.saas.hpe.com", 37001L, 5005L, "cocian@hpe.com", "Dragon123")
                        ));


        serviceModule.getInstance(MetadataService.class).getFormLayoutForAllEntityTypes();
    }

}
