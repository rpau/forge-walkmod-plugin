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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.maven.plugins.ExecutionBuilder;
import org.jboss.forge.addon.maven.plugins.MavenPluginImpl;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
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
import org.jboss.forge.furnace.util.Streams;

public class WalkmodSetupCommand extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	@WithAttributes(label = "embedded", type = InputType.CHECKBOX, defaultValue = "true", description = "Adds the maven-walkmod-plugin into the pom.xml to run walkmod in an embedded mode")
	private UIInput<Boolean> embedded;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(WalkmodSetupCommand.class).name(
				"walkmod-setup");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		builder.add(embedded);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		Project project = getSelectedProject(context);
		if (project.hasFacet(ResourcesFacet.class)) {
			File fileResource = new File("walkmod.xml");
			if (!fileResource.exists()) {

				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("/template/walkmod.xml");
				if (is == null) {
					return Results
							.fail("The template 'template/walkmod.xml' is not found");
				}
				fileResource.createNewFile();
				FileOutputStream os = new FileOutputStream(fileResource);
				try {
					Streams.write(is, os);
				} finally {
					is.close();
					os.close();
				}

				if (project.hasFacet(MavenPluginFacet.class)
						&& embedded.getValue()) {
					MavenPluginFacet facet = project
							.getFacet(MavenPluginFacet.class);
					MavenPluginImpl mp = new MavenPluginImpl();
					CoordinateBuilder coordinate = CoordinateBuilder.create()
							.setGroupId("org.walkmod")
							.setArtifactId("maven-walkmod-plugin")
							.setVersion("1.3");
					mp.setCoordinate(coordinate);
					mp.addExecution(ExecutionBuilder.create()
							.setPhase("generate-sources").addGoal("apply"));
					facet.addPlugin(mp);
				}
			}
			return Results
					.success("Command 'walkmod-setup' successfully executed!");
		} else {
			return Results.fail("Invalid resource facet");
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
