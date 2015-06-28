package com.redowlanalytics.swagger2markup;

import io.github.robwin.markup.builder.MarkupLanguage;
import io.github.robwin.swagger2markup.Swagger2MarkupConverter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * Basic mojo to invoke the {@link Swagger2MarkupConverter}
 * during the maven build cycle
 */
@Mojo(name = "process-swagger")
public class Swagger2MarkupMojo extends AbstractMojo {

    private static final String PREFIX = "swagger2markup.";

    @Parameter(property = PREFIX + "inputDirectory", defaultValue = "${project.basedir}/src/docs/swagger", required = true)
    protected File inputDirectory;

    @Parameter(property = PREFIX + "outputDirectory", required = true)
    protected File outputDirectory;

    @Parameter(property = PREFIX + "markupLanguage", defaultValue = "asciidoc", required = true)
    protected String markupLanguage = "asciidoc";

    @Parameter(property = PREFIX + "examplesDirectory", required = false)
    protected File examplesDirectory;

    @Parameter(property = PREFIX + "descriptionsDirectory", required = false)
    protected File descriptionsDirectory;

    @Parameter(property = PREFIX + "schemasDirectory", required = false)
    protected File schemasDirectory;

    @Parameter(property = PREFIX + "separateDefinitions", required = false)
    protected Boolean separateDefinitions;

    @Parameter(defaultValue = "${project.build.directory}")
    protected String projectBuildDir;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (getLog().isDebugEnabled()) {
            getLog().debug("convertSwagger2markup task started");
            getLog().debug("InputDir: " + inputDirectory);
            getLog().debug("OutputDir: " + outputDirectory);
            getLog().debug("ExamplesDir: " + examplesDirectory);
            getLog().debug("DescriptionsDir: " + descriptionsDirectory);
            getLog().debug("SchemasDir: " + schemasDirectory);
            getLog().debug("MarkupLanguage: " + markupLanguage);
            getLog().debug("SeparateDefinitions: " + separateDefinitions);
        }

        final MarkupLanguage markupLanguageEnum = MarkupLanguage.valueOf(markupLanguage.toUpperCase());
        if(outputDirectory == null) {
            outputDirectory = new File(projectBuildDir, markupLanguage.toLowerCase());
        }

        final File[] files = inputDirectory.listFiles();
        if(files == null || files.length == 0) {
            throw new MojoFailureException("No swagger files found in directory: " + inputDirectory);
        }

        for(File file : files) {
            if (getLog().isDebugEnabled()) {
                getLog().debug("File: " + file.getAbsolutePath());
            }
            final Swagger2MarkupConverter.Builder builder = Swagger2MarkupConverter
                    .from(file.getAbsolutePath())
                    .withMarkupLanguage(markupLanguageEnum);
            if(examplesDirectory != null){
                getLog().debug("Include examples is enabled.");
                builder.withExamples(examplesDirectory.getAbsolutePath());
            }
            if(descriptionsDirectory != null){
                getLog().debug("Include descriptions is enabled.");
                builder.withDescriptions(descriptionsDirectory.getAbsolutePath());
            }
            if(schemasDirectory != null){
                getLog().debug("Include schemas is enabled.");
                builder.withSchemas(schemasDirectory.getAbsolutePath());
            }
            if(separateDefinitions != null && separateDefinitions) {
                getLog().debug("Separate definitions enabled.");
                builder.withSeparatedDefinitions();
            }
            try {
                builder.build().intoFolder(outputDirectory.getAbsolutePath());
            } catch (IOException e) {
                throw new MojoFailureException("Failed to write markup to directory: " + outputDirectory, e);
            }
        }
        getLog().debug("convertSwagger2markup task finished");
    }
}
