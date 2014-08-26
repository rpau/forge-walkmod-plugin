package org.walkmod.forge;

import org.jboss.arquillian.container.test.api.Deployment;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WalkmodAddonTest {

	@Deployment
	public static ForgeArchive getDeployment() {
		ForgeArchive archive = ShrinkWrap.create(ForgeArchive.class);
		return archive;
	}

	@Test
	public void testSomething() throws Exception {
		Assert.fail("Not implemented");
	}
}
