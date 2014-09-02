package org.walkmod.forge.addon.commands;

import java.io.File;
import java.util.ArrayList;

import javax.inject.Inject;

import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.ui.AbstractProjectCommand;
import org.jboss.forge.addon.ui.context.UIBuilder;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.hints.InputType;
import org.jboss.forge.addon.ui.input.UIInput;
import org.jboss.forge.addon.ui.input.UIInputMany;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.metadata.WithAttributes;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.result.Results;
import org.jboss.forge.addon.ui.util.Metadata;
import org.walkmod.WalkModFacade;

import com.google.common.collect.Lists;

public class WalkmodApplyCommand extends AbstractProjectCommand {

	@Inject
	private ProjectFactory projectFactory;

	@Inject
	@WithAttributes(label = "verbose", type = InputType.CHECKBOX, defaultValue = "true", description = "Runs Walkmod in verbose mode", required = false)
	private UIInput<Boolean> verbose;

	@Inject
	@WithAttributes(label = "printError", type = InputType.CHECKBOX, defaultValue = "false", description = "Print the Walkmod stacktrace when an error appears", required = false)
	private UIInput<Boolean> printError;

	@Inject
	@WithAttributes(label = "chains", description = "walkmod chains to apply. If no chain is selected, Walkmod will apply all declared chains", required = false)
	private UIInputMany<String> chains;
	
	@Inject
	@WithAttributes(label = "offline", type = InputType.CHECKBOX, defaultValue = "false", description = "Run walkmod without downloading plugins", required = false)
	private UIInput<Boolean> offline;

	@Override
	public UICommandMetadata getMetadata(UIContext context) {
		return Metadata.forCommand(WalkmodApplyCommand.class).name(
				"walkmod-apply");
	}

	@Override
	public void initializeUI(UIBuilder builder) throws Exception {
		 builder.add(chains).add(printError).add(verbose).add(offline);
	}

	@Override
	public Result execute(UIExecutionContext context) throws Exception {
		
		File walkmodCfgFile = new File("walkmod.xml");

		if (walkmodCfgFile.exists()) {
			WalkModFacade walkmod = new WalkModFacade(walkmodCfgFile, false,
					verbose.getValue(), printError.getValue());
			if (!chains.hasValue()) {
				walkmod.apply();
			}
			else{
				ArrayList<String> chainList = Lists.newArrayList(chains.getValue());
				String[] chains = new String[chainList.size()];
				chainList.toArray(chains);
				walkmod.apply(chains);
			}
			return Results
					.success("Command 'walkmod-apply' successfully executed!");
		} else {
			return Results
					.fail("Command 'walkmod-apply' fails. You need apply 'walkmod-setup' first.");
		}

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