package org.walkmod.forge.addon.commands;

import javax.inject.Inject;

import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.resources.JavaResource;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.context.UIValidationContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.jboss.forge.addon.ui.validate.UIValidator;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;

public class WalkmodNewVisitorCommand extends AbstractProjectCommand {
	@Inject
	@WithAttributes(label = "visitor class", type = InputType.JAVA_CLASS_PICKER)
	private UIInput<JavaResource> visitor;

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

		visitor.addValidator(new UIValidator() {

			@Override
			public void validate(UIValidationContext context) {
				JavaResource clazz = (JavaResource) context
						.getCurrentInputComponent().getValue();
				if (clazz.exists()) {
					context.addValidationError(
							context.getCurrentInputComponent(), "The class "
									+ clazz.getFullyQualifiedName()
									+ " already exists.");
				}

			}
		});
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {

		JavaResource javaResource = visitor.getValue();

		String fqn = javaResource.getFullyQualifiedName();

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
			source.addImport("org.walkmod.visitors.VisitorSupport");
			source.addImport("org.walkmod.javalang.ast.CompilationUnit");
			source.addImport("org.walkmod.walkers.VisitorContext");

			MethodSource<JavaClassSource> method = source.addMethod("visit");
			method.addParameter("CompilationUnit", "cu");
			method.addParameter("VisitorContext", "context");
			method.setPublic().setReturnType(Void.class);

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