/*  Copyright (C) 2014 Raquel Pau and Albert Coroleu.
 
  Forge Walkmod Plugin is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  Forge Walkmod Plugin is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.forge.addon.commands;

import javax.inject.Inject;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

public class WalkmodNewVisitorCommand extends AbstractProjectCommand {

	@Inject
	@WithAttributes(label = "visitor class", type = InputType.DEFAULT)
	private UIInput<String> visitor;

	@Inject
	private ProjectFactory projectFactory;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(WalkmodNewVisitorCommand.class).name(
				"walkmod-new-visitor");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		builder.add(visitor);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		String fqn = visitor.getValue();
		String packageName = org.jboss.forge.roaster.model.util.Types
				.getPackage(fqn);
		String typeName = org.jboss.forge.roaster.model.util.Types
				.toSimpleName(fqn);
		Project project = getSelectedProject(context);
		if (project.hasFacet(JavaSourceFacet.class)) {
			JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
			JavaClassSource source = Roaster.create(JavaClassSource.class)
					.setPackage(packageName).setName(typeName);
			source.setSuperType("VisitorSupport");
			source.addImport("org.walkmod.javalang.visitors.VisitorSupport");
			source.addImport("org.walkmod.javalang.ast.CompilationUnit");
			source.addImport("org.walkmod.walkers.VisitorContext");
			MethodSource<JavaClassSource> method = source.addMethod();
			method.setName("visit");
			method.addParameter("CompilationUnit", "cu");
			method.addParameter("VisitorContext", "context");
			method.setBody("\n");
			method.setPublic().setReturnType("void");
			facet.saveJavaSource(source);			
			return Results
					.success("Command 'walkmod-new-visitor' successfully executed!");
		} else {
			return Results
					.fail("Command 'walkmod-new-visitor' fails. This is not a java project");
		}
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}
}
