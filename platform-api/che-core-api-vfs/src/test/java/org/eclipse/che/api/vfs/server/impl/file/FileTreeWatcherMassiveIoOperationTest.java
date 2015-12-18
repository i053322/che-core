/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server.impl.file;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.NameGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FileTreeWatcherMassiveIoOperationTest {
    private FileTreeWatcher fileTreeWatcher;
    private File            testDirectory;
    private TestedFileTree  testedFileTree;

    @Before
    public void setUp() throws Exception {
        File targetDir = new File(Thread.currentThread().getContextClassLoader().getResource(".").getPath()).getParentFile();
        testDirectory = new File(targetDir, NameGenerator.generate("watcher-", 4));
        assertTrue(testDirectory.mkdir());
        testedFileTree = new TestedFileTree(testDirectory);
    }

    @After
    public void tearDown() throws Exception {
        if (fileTreeWatcher != null) {
            fileTreeWatcher.shutdown();
        }
        IoUtil.deleteRecursive(testDirectory);
    }

    @Test
    public void watchesTreeCreation() throws Exception {
        FileWatcherNotificationListener notificationListener = aNotificationListener();
        fileTreeWatcher = new FileTreeWatcher(testDirectory, newArrayList(), notificationListener);
        fileTreeWatcher.startup();
        Thread.sleep(500);

        List<String> allFilesAndDirs = testedFileTree.createTree("", 7, 5);

        Thread.sleep(3000);

        verify(notificationListener, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationListener, never()).pathDeleted(eq(testDirectory), anyString(), anyBoolean());
        verify(notificationListener, never()).pathUpdated(eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> createdEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(allFilesAndDirs.size())).pathCreated(eq(testDirectory), createdEvents.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(createdEvents.getAllValues(), allFilesAndDirs);
    }

    @Test
    public void watchesTreeDeletion() throws Exception {
        List<String> allFilesAndDirs = testedFileTree.createTree("", 7, 5);
        Thread.sleep(100);

        FileWatcherNotificationListener notificationListener = aNotificationListener();
        fileTreeWatcher = new FileTreeWatcher(testDirectory, newArrayList(), notificationListener);
        fileTreeWatcher.startup();
        Thread.sleep(3000);

        assertTrue(testedFileTree.delete(""));

        Thread.sleep(3000);

        verify(notificationListener, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationListener, never()).pathCreated(eq(testDirectory), anyString(), anyBoolean());
        verify(notificationListener, never()).pathUpdated(eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> deletedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(allFilesAndDirs.size())).pathDeleted(eq(testDirectory), deletedEvents.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(deletedEvents.getAllValues(), allFilesAndDirs);
    }

    @Test
    public void watchesUpdatesAllFilesInTree() throws Exception {
        testedFileTree.createTree("", 7, 5);
        Thread.sleep(100);

        FileWatcherNotificationListener notificationListener = aNotificationListener();
        fileTreeWatcher = new FileTreeWatcher(testDirectory, newArrayList(), notificationListener);
        fileTreeWatcher.startup();
        Thread.sleep(3000);

        List<String> updated = testedFileTree.findAllFilesInTree("");

        for (String file : updated) {
            testedFileTree.updateFile(file);
        }

        Thread.sleep(3000);

        verify(notificationListener, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationListener, never()).pathCreated(eq(testDirectory), anyString(), anyBoolean());
        verify(notificationListener, never()).pathDeleted(eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> updatedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(updated.size())).pathUpdated(eq(testDirectory), updatedEvents.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(updatedEvents.getAllValues(), updated);
    }


    @Test
    public void watchesUpdatesFilesInTree() throws Exception {
        testedFileTree.createTree("", 7, 5);
        Thread.sleep(100);

        FileWatcherNotificationListener notificationListener = aNotificationListener();
        fileTreeWatcher = new FileTreeWatcher(testDirectory, newArrayList(), notificationListener);
        fileTreeWatcher.startup();
        Thread.sleep(3000);

        List<String> updated = testedFileTree.findAllFilesInTree("").stream()
                                             .filter(path -> path.hashCode() % 2 == 0).collect(Collectors.toList());

        for (String file : updated) {
            testedFileTree.updateFile(file);
        }

        Thread.sleep(3000);

        verify(notificationListener, never()).errorOccurred(eq(testDirectory), any(Throwable.class));
        verify(notificationListener, never()).pathCreated(eq(testDirectory), anyString(), anyBoolean());
        verify(notificationListener, never()).pathDeleted(eq(testDirectory), anyString(), anyBoolean());

        ArgumentCaptor<String> updatedEvents = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(updated.size())).pathUpdated(eq(testDirectory), updatedEvents.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(updatedEvents.getAllValues(), updated);
    }

    @Test
    public void watchesMixedActionsInTree() throws Exception {
        testedFileTree.createTree("", 7, 5);
        Thread.sleep(100);

        FileWatcherNotificationListener notificationListener = aNotificationListener();
        fileTreeWatcher = new FileTreeWatcher(testDirectory, newArrayList(), notificationListener);
        fileTreeWatcher.startup();
        Thread.sleep(3000);

        List<String> allFiles = testedFileTree.findAllFilesInTree("");
        List<String> updated = newArrayList(allFiles.subList(0, allFiles.size() / 2));
        List<String> deleted = newArrayList(allFiles.subList(allFiles.size() / 2, allFiles.size()));
        List<String> directories = testedFileTree.findAllDirectoriesInTree("");
        List<String> created = newArrayList();

        for (String directory : directories) {
            created.add(testedFileTree.createFile(directory));
        }

        for (String file : deleted) {
            testedFileTree.delete(file);
        }

        Thread.sleep(3000);

        updated.addAll(created.subList(0, created.size() / 2));
        for (String file : updated) {
            testedFileTree.updateFile(file);
        }

        Thread.sleep(3000);

        verify(notificationListener, never()).errorOccurred(eq(testDirectory), any(Throwable.class));

        ArgumentCaptor<String> eventsCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(deleted.size())).pathDeleted(eq(testDirectory), eventsCaptor.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(eventsCaptor.getAllValues(), deleted);

        eventsCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(updated.size())).pathUpdated(eq(testDirectory), eventsCaptor.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(eventsCaptor.getAllValues(), updated);

        eventsCaptor = ArgumentCaptor.forClass(String.class);
        verify(notificationListener, times(created.size())).pathCreated(eq(testDirectory), eventsCaptor.capture(), anyBoolean());
        assertThatCollectionsContainsSameItemsOrFailWithDiff(eventsCaptor.getAllValues(), created);
    }

    private FileWatcherNotificationListener aNotificationListener() {
        return mock(FileWatcherNotificationListener.class);
    }

    private void assertThatCollectionsContainsSameItemsOrFailWithDiff(Collection<String> actual, Collection<String> expected) {
        List<String> missed = newArrayList(expected);
        List<String> extra = newArrayList(actual);
        missed.removeAll(actual);
        extra.removeAll(expected);
        if (missed.isEmpty() && extra.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder();
        if (missed.size() > 0) {
            message.append("\n>>> Expected items:\n")
                   .append(missed).append('\n').append("but missed\n");
        }
        if (extra.size() > 0) {
            message.append("\n>>> Items:\n")
                   .append(extra).append('\n').append("not expected but found\n");
        }
        fail(message.toString());
    }
}