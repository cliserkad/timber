package xyz.cliserkad;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Mojo(name = "timber", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.NONE, requiresOnline = false, requiresProject = true)
@Execute(goal = "init", phase = LifecyclePhase.INITIALIZE)
public class LogLevelDetector extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException {
		int level = -1;
		if(getLog().isErrorEnabled())
			level++;
		if(getLog().isWarnEnabled())
			level++;
		if(getLog().isInfoEnabled())
			level++;
		if(getLog().isDebugEnabled())
			level++;
		getLog().info("Output level: " + level);
	}

}
