package xyz.cliserkad.timber.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.plugins.annotations.*;

@Mojo(
	name = "level",
	defaultPhase = LifecyclePhase.NONE,
	requiresDependencyResolution = ResolutionScope.NONE,
	requiresOnline = false,
	requiresProject = true
)
@Execute(
	goal = "level",
	phase = LifecyclePhase.NONE
)
public class LogLevelDetector extends AbstractMojo {

	@Parameter(
		defaultValue = "${project}",
		readonly = true,
		required = true
	)
	private MavenProject project;

	@Parameter(
		defaultValue = "${session}",
		readonly = true,
		required = true
	)
	private MavenSession session;

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
		project.getProperties().setProperty("timber.level", Integer.toString(level));
		session.getUserProperties().setProperty("timber.level", Integer.toString(level));
		session.getSystemProperties().setProperty("timber.level", Integer.toString(level));
	}

}
