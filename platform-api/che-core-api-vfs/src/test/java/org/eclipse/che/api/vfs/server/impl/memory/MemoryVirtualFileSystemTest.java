/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.vfs.server.impl.memory;

import org.eclipse.che.api.vfs.server.ArchiverFactory;
import org.eclipse.che.api.vfs.server.VirtualFileSystem;
import org.eclipse.che.api.vfs.server.search.Searcher;
import org.eclipse.che.api.vfs.server.search.SearcherProvider;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MemoryVirtualFileSystemTest {
    private MemoryVirtualFileSystem         fileSystem;
    private Searcher                        searcher;
    private VirtualFileSystem.CloseCallback closeCallback;

    @Before
    public void setUp() throws Exception {
        SearcherProvider searcherProvider = mock(SearcherProvider.class);
        searcher = mock(Searcher.class);
        closeCallback = mock(VirtualFileSystem.CloseCallback.class);
        fileSystem = new MemoryVirtualFileSystem(mock(ArchiverFactory.class), searcherProvider, closeCallback);
        when(searcherProvider.getSearcher(eq(fileSystem), anyBoolean())).thenReturn(searcher);
    }

    @Test
    public void notifiedCallbackWhenFileSystemClosed() throws Exception {
        fileSystem.close();
        verify(closeCallback).onClose();
    }

    @Test
    public void closesSearcherWhenFileSystemClosed() throws Exception {
        fileSystem.close();
        verify(searcher).close();
    }
}