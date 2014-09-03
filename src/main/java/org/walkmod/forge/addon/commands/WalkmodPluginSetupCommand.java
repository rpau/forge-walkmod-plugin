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
import java.io.InputStream;

import javax.inject.Inject;

import org.jboss.forge.addon.dependencies.Dependency;
import org.jboss.forge.addon.dependencies.builder.DependencyBuilder;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.DependencyFacet;
import org.jboss.forge.addon.projects.facets.ResourcesFacet;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;

public class WalkmodPluginSetupCommand extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(WalkmodPluginSetupCommand.class).name(
				"walkmod-plugin-setup");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
	}

	@SuppressWarnings("unchecked")
	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		Project project = getSelectedProject(context);

		if (project.hasAllFacets(ResourcesFacet.class, MavenFacet.class)) {

			MavenFacet maven = project.getFacet(MavenFacet.class);

			String artifactId = maven.getModel().getArtifactId();
			if (artifactId.startsWith("walkmod-")
					&& artifactId.endsWith("-plugin")) {

				ResourcesFacet resources = project
						.getFacet(ResourcesFacet.class);

				FileResource<?> pluginDescriptor = resources
						.getResource("META-INF" + File.separator + "walkmod"
								+ File.separator + artifactId + ".xml");
				pluginDescriptor.createNewFile();

				InputStream is = this.getClass().getClassLoader()
						.getResourceAsStream("/template/plugin-descriptor.xml");
				try {
					pluginDescriptor.setContents(is);
				} finally {
					is.close();
				}

			} else {

				if (artifactId.startsWith("walkmod-")) {
					artifactId = artifactId.substring("walkmod-".length());
				}
				if (artifactId.endsWith("-plugin")) {
					artifactId = artifactId.substring(0,
							artifactId.lastIndexOf("-plugin"));
				}

				Results.fail("Invalid artifactId. It must be  'walkmod-"
						+ artifactId + "-plugin");
			}

		}
		if (project.hasFacet(DependencyFacet.class)) {
			DependencyFacet mvnFacet = project.getFacet(DependencyFacet.class);
			Dependency mp = DependencyBuilder.create()
					.setGroupId("org.walkmod")
					.setArtifactId("walkmod-javalang-plugin")
					.setVersion("LATEST");
			mvnFacet.addDirectDependency(mp);
		}
		return Results
				.success("Command 'walkmod-plugin-setup' successfully executed!");
	}

	@Override
	protected boolean isProjectRequired() {
		return true;
	}

	@Override
	protected ProjectFactory getProjectFactory() {
		return projectFactory;
	}
}
