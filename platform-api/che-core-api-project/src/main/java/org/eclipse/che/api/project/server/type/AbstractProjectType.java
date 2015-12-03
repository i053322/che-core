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
package org.eclipse.che.api.project.server.type;

import org.eclipse.che.api.core.model.project.type.Attribute;
import org.eclipse.che.api.core.model.project.type.ProjectType;
import org.eclipse.che.api.project.server.ValueProviderFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gazarenkov
 */
public abstract class AbstractProjectType implements ProjectType {

    protected final boolean                persisted;
    private final   String                 id;
    private final   String                 displayName;
    private final   Map<String, Attribute> attributes;
    private final   List<ProjectType>      parents;
    private final   boolean                mixable;
    private final   boolean                primaryable;

    protected AbstractProjectType(String id, String displayName, boolean primaryable, boolean mixable, boolean persisted) {
        this.id = id;
        this.displayName = displayName;
        this.attributes = new HashMap<>();
        this.parents = new ArrayList<>();
        this.mixable = mixable;
        this.primaryable = primaryable;
        this.persisted = persisted;
    }

    /**
     * @param id
     * @param displayName
     * @param primaryable
     *         - whether the ProjectType can be used as Primary
     * @param mixable
     *         - whether the projectType can be used as Mixin
     */
    protected AbstractProjectType(String id, String displayName, boolean primaryable, boolean mixable) {
        this(id, displayName, primaryable, mixable, true);
    }

    public boolean isPersisted() {
        return persisted;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<>(attributes.values());
    }

    public List<ProjectType> getParents() {
        return parents;
    }

    public boolean isTypeOf(String typeId) {
        if (this.id.equals(typeId)) {
            return true;
        }

        return recurseParents(this, typeId);
    }

    public Attribute getAttribute(String name) {
        return attributes.get(name);
    }

    public boolean canBeMixin() {
        return mixable;
    }

    public boolean canBePrimary() {
        return primaryable;
    }

    protected void addConstantDefinition(String name, String description, AttributeValue value) {
        attributes.put(name, new Constant(id, name, description, value));
    }

    protected void addConstantDefinition(String name, String description, String value) {
        attributes.put(name, new Constant(id, name, description, value));
    }

    protected void addVariableDefinition(String name, String description, boolean required) {
        attributes.put(name, new Variable(id, name, description, required));
    }

    protected void addVariableDefinition(String name, String description, boolean required, AttributeValue value) {
        attributes.put(name, new Variable(id, name, description, required, value));
    }

    protected void addVariableDefinition(String name, String description, boolean required, ValueProviderFactory factory) {
        attributes.put(name, new Variable(id, name, description, required, factory));
    }

    protected void addAttributeDefinition(Attribute attr) {
        attributes.put(attr.getName(), attr);
    }

    protected void addParent(AbstractProjectType parent) {
        parents.add(parent);
    }

    private boolean recurseParents(ProjectType child, String parent) {

        for (ProjectType p : child.getParents()) {
            if (p.getId().equals(parent)) {
                return true;
            }
            if (recurseParents(p, parent)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String getDefaultRecipe() {
        return "";
    }
}