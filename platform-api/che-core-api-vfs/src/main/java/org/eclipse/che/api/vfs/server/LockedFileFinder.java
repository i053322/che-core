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
package org.eclipse.che.api.vfs.server;

import org.eclipse.che.api.core.ServerException;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class LockedFileFinder implements VirtualFileVisitor {
    private final VirtualFile       folder;
    private final List<VirtualFile> locked;

    public LockedFileFinder(VirtualFile folder) {
        this.folder = folder;
        locked = newArrayList();
    }

    public List<VirtualFile> findLockedFiles() throws ServerException {
        folder.accept(this);
        return locked;
    }

    @Override
    public void visit(VirtualFile virtualFile) throws ServerException {
        if (virtualFile.isFolder()) {
            for (VirtualFile child : virtualFile.getChildren()) {
                child.accept(this);
            }
        }
        if (virtualFile.isFile() && virtualFile.isLocked()) {
            locked.add(virtualFile);
        }
    }
}