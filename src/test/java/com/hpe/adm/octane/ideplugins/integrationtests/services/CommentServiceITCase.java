/*******************************************************************************
 * Copyright 2017-2023 Open Text.
 *
 * The only warranties for products and services of Open Text and
 * its affiliates and licensors (“Open Text”) are as may be set forth
 * in the express warranty statements accompanying such products and services.
 * Nothing herein should be construed as constituting an additional warranty.
 * Open Text shall not be liable for technical or editorial errors or
 * omissions contained herein. The information contained herein is subject
 * to change without notice.
 *
 * Except as specifically indicated otherwise, this document contains
 * confidential information and a valid license is required for possession,
 * use or copying. If this work is provided to the U.S. Government,
 * consistent with FAR 12.211 and 12.212, Commercial Computer Software,
 * Computer Software Documentation, and Technical Data for Commercial Items are
 * licensed to the U.S. Government under vendor's standard commercial license.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hpe.adm.octane.ideplugins.integrationtests.services;


import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.hpe.adm.nga.sdk.model.EntityModel;
import com.hpe.adm.octane.ideplugins.integrationtests.TestServiceModule;
import com.hpe.adm.octane.ideplugins.integrationtests.util.EntityUtils;
import com.hpe.adm.octane.ideplugins.services.CommentService;
import com.hpe.adm.octane.ideplugins.services.EntityService;
import com.hpe.adm.octane.ideplugins.services.di.ServiceModule;
import com.hpe.adm.octane.ideplugins.services.filtering.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.UUID;

public class CommentServiceITCase {

    @Inject
    private CommentService commentService;

    @Inject
    private EntityService entityService;

    @Inject
    private EntityUtils entityUtils;

    @Before
    public void setUp() {
        ServiceModule serviceModule = TestServiceModule.getServiceModule();
        Injector injector = Guice.createInjector(serviceModule);
        injector.injectMembers(this);
    }


    @Test
    public void testCommentService() {

        try {
            EntityModel userStory = entityUtils.createEntity(Entity.USER_STORY);

            //Add a random comment
            String commentText = "Test comment" + UUID.randomUUID().toString();
            commentService.postComment(userStory, commentText);

            //Retrieve comments
            Collection<EntityModel> comments = commentService.getComments(userStory);

            //Check if comment was posted
            EntityModel postedComment = comments.iterator().next();
            String postedCommentText = postedComment.getValue("text").getValue().toString();
            Assert.assertTrue(postedCommentText != null && postedCommentText.contains(commentText));

            commentService.deleteComment(postedComment.getValue("id").getValue().toString());

            comments = commentService.getComments(userStory);

            //Check if comment was deleted
            Assert.assertTrue(comments
                    .stream()
                    .map(comment -> comment.getValue("text").getValue().toString())
                    .filter(text -> text != null)
                    .noneMatch(text -> text.contains(commentText))
            );

            entityUtils.deleteEntityModel(userStory);
        } catch (Exception ex) {
            Assert.fail("Failed to carry out comment test: " + ex.getMessage());
        }


    }

}
