package uk.gemwire.waitress.web;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.SimpleLoggerFactory;
import uk.gemwire.waitress.Waitress;
import uk.gemwire.waitress.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MavenDownloader {
    private static final RepositorySystem REPOSITORY_SYSTEM;
    private static final RepositorySystemSession SESSION;
    private static RemoteRepository REMOTE;

    public static void setRemote(String id, String type, String url){
        assert !url.isEmpty();
        REMOTE =  new RemoteRepository.Builder(id, type, url).build();
    }
    static {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(ILoggerFactory.class, SimpleLoggerFactory.class);
        REPOSITORY_SYSTEM = locator.getService(RepositorySystem.class);

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(Config.DATA_DIR);
        session.setLocalRepositoryManager(REPOSITORY_SYSTEM.newLocalRepositoryManager(session, localRepo));
        SESSION = session;
    }

    public static File getArtifact(String groupID, String artifactID, String version, String classifier, String extension) throws IOException {
        Artifact artifact = new DefaultArtifact(groupID, artifactID, classifier, extension, version);
        ArtifactRequest request = new ArtifactRequest();
        request.setArtifact(artifact);
        List<RemoteRepository> repositories = new ArrayList<>();
        // TODO Implement authentication
        repositories.add(REMOTE);
        request.setRepositories(repositories);
        File downloaded = null;
        try {
            ArtifactResult result = REPOSITORY_SYSTEM.resolveArtifact(SESSION, request);
            artifact = result.getArtifact();
            if (artifact!=null) downloaded = artifact.getFile();
        } catch (ArtifactResolutionException e) {
            if (e.getResult().isMissing()) {
                Waitress.LOGGER.warn("Artifact " + groupID + "/" + artifactID + "/" + version + "/" + artifactID + "-" + version + classifier + "." + extension + " is missing!");
            }else e.printStackTrace();
        }
        return downloaded;
    }
}
