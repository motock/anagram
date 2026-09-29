package com.example.anagram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Structural checks for the Maven scaffold required by this story.
 *
 * <p>These assertions are deliberately membership based (a required element is
 * present with the required value) rather than exact-content based, so that
 * later stories can extend {@code pom.xml} and {@code src/main/java} without
 * breaking this suite.</p>
 */
class ProjectStructureTests {

    private static final String SPRING_BOOT_GROUP = "org.springframework.boot";
    private static final String APPLICATION_SOURCE = "src/main/java/com/example/anagram/AnagramApplication.java";
    private static final String GITIGNORE = ".gitignore";

    // ------------------------------------------------------------------
    // .gitignore
    // ------------------------------------------------------------------

    @Test
    @DisplayName(".gitignore ignores .DS_Store at every level and keeps the pre-existing entries")
    void gitignoreIgnoresDsStoreEverywhere() throws IOException {
        Path gitignore = projectRoot().resolve(GITIGNORE);
        assertTrue(Files.isRegularFile(gitignore), ".gitignore must exist at the repository root, expected: " + gitignore);

        List<String> lines = Files.readAllLines(gitignore).stream()
                .map(String::trim)
                .toList();

        // Exact equality on the trimmed line is deliberate: a root-anchored "/.DS_Store"
        // (which would leave src/.DS_Store trackable) must not satisfy this.
        assertTrue(lines.contains(".DS_Store"),
                ".gitignore must contain an unanchored '.DS_Store' line so that .DS_Store at the repository root "
                        + "and nested files such as src/.DS_Store are both ignored; actual lines: " + lines);

        for (String required : List.of("target/", "*.class", "*.jar", "test_author.log", "deps.txt")) {
            assertTrue(lines.contains(required),
                    ".gitignore must keep the pre-existing entry '" + required + "'; actual lines: " + lines);
        }
    }

    // ------------------------------------------------------------------
    // pom.xml
    // ------------------------------------------------------------------

    @Test
    @DisplayName("pom.xml exists at the repository root")
    void pomXmlExistsAtRepositoryRoot() {
        Path pom = projectRoot().resolve("pom.xml");
        assertTrue(Files.isRegularFile(pom), "pom.xml must exist at the repository root, expected: " + pom);
    }

    @Test
    @DisplayName("pom.xml inherits from spring-boot-starter-parent 4.1.1 with an empty <relativePath/>")
    void pomDeclaresSpringBootParent() throws Exception {
        Element project = pomDocument().getDocumentElement();
        assertEquals("project", localName(project), "pom.xml root element must be <project>");

        Element parent = child(project, "parent");
        assertNotNull(parent, "pom.xml must declare a <parent> element");
        assertEquals(SPRING_BOOT_GROUP, text(parent, "groupId"), "<parent><groupId> must be org.springframework.boot");
        assertEquals("spring-boot-starter-parent", text(parent, "artifactId"), "<parent><artifactId> must be spring-boot-starter-parent");
        assertEquals("4.1.1", text(parent, "version"), "<parent><version> must be 4.1.1");

        Element relativePath = child(parent, "relativePath");
        assertNotNull(relativePath, "<parent> must contain an empty <relativePath/> element");
        assertEquals("", relativePath.getTextContent().trim(), "<relativePath/> must be empty");
    }

    @Test
    @DisplayName("pom.xml declares the project coordinates com.example:anagram:0.0.1-SNAPSHOT (jar)")
    void pomDeclaresProjectCoordinates() throws Exception {
        Element project = pomDocument().getDocumentElement();
        assertEquals("com.example", text(project, "groupId"), "<groupId> must be com.example");
        assertEquals("anagram", text(project, "artifactId"), "<artifactId> must be anagram");
        assertEquals("0.0.1-SNAPSHOT", text(project, "version"), "<version> must be 0.0.1-SNAPSHOT");
        assertEquals("jar", text(project, "packaging"), "<packaging> must be jar");
    }

    @Test
    @DisplayName("pom.xml sets <java.version>21</java.version>")
    void pomSetsJavaVersionTo21() throws Exception {
        Element project = pomDocument().getDocumentElement();
        Element properties = child(project, "properties");
        assertNotNull(properties, "pom.xml must declare a <properties> section");
        assertEquals("21", text(properties, "java.version"), "<properties><java.version> must be 21");
    }

    @Test
    @DisplayName("pom.xml declares the three required starters with no explicit versions")
    void pomDeclaresRequiredStartersWithoutExplicitVersions() throws Exception {
        Element dependencies = dependenciesElement();
        for (String artifactId : List.of(
                "spring-boot-starter-web",
                "spring-boot-starter-validation",
                "spring-boot-starter-test")) {
            Element dependency = findDependency(dependencies, artifactId);
            assertNotNull(dependency, "pom.xml must declare a dependency on " + SPRING_BOOT_GROUP + ":" + artifactId);
            assertEquals(SPRING_BOOT_GROUP, text(dependency, "groupId"),
                    artifactId + " must have <groupId>" + SPRING_BOOT_GROUP + "</groupId>");
            assertNull(child(dependency, "version"),
                    artifactId + " must not declare an explicit <version> - it is managed by the parent");
        }
    }

    @Test
    @DisplayName("no Spring Boot dependency pins its own version")
    void noSpringBootDependencyOverridesTheManagedVersion() throws Exception {
        List<String> bootArtifactIds = new ArrayList<>();
        List<String> offenders = new ArrayList<>();
        for (Element dependency : children(dependenciesElement(), "dependency")) {
            if (!SPRING_BOOT_GROUP.equals(text(dependency, "groupId"))) {
                continue;
            }
            String artifactId = text(dependency, "artifactId");
            bootArtifactIds.add(artifactId);
            if (child(dependency, "version") != null) {
                offenders.add(artifactId + ":" + text(dependency, "version"));
            }
        }

        assertFalse(bootArtifactIds.isEmpty(),
                "expected at least one " + SPRING_BOOT_GROUP + " dependency, otherwise this check passes vacuously");
        assertTrue(offenders.isEmpty(),
                "every " + SPRING_BOOT_GROUP + " dependency must inherit its version from the parent pom; these pin one instead: "
                        + offenders);
    }

    @Test
    @DisplayName("spring-boot-starter-test is scoped to test")
    void springBootStarterTestIsTestScoped() throws Exception {
        Element dependency = findDependency(dependenciesElement(), "spring-boot-starter-test");
        assertNotNull(dependency, "pom.xml must declare a dependency on " + SPRING_BOOT_GROUP + ":spring-boot-starter-test");
        assertEquals("test", text(dependency, "scope"), "spring-boot-starter-test must declare <scope>test</scope>");
    }

    @Test
    @DisplayName("pom.xml declares the spring-boot-maven-plugin build plugin")
    void pomDeclaresSpringBootMavenPlugin() throws Exception {
        Element project = pomDocument().getDocumentElement();
        Element build = child(project, "build");
        assertNotNull(build, "pom.xml must declare a <build> section");
        Element plugins = child(build, "plugins");
        assertNotNull(plugins, "pom.xml must declare a <build><plugins> section");

        Element plugin = null;
        for (Element candidate : children(plugins, "plugin")) {
            if ("spring-boot-maven-plugin".equals(text(candidate, "artifactId"))) {
                plugin = candidate;
                break;
            }
        }
        assertNotNull(plugin, "pom.xml must declare the " + SPRING_BOOT_GROUP + ":spring-boot-maven-plugin build plugin");
        assertEquals(SPRING_BOOT_GROUP, text(plugin, "groupId"),
                "spring-boot-maven-plugin must have <groupId>" + SPRING_BOOT_GROUP + "</groupId>");
    }

    // ------------------------------------------------------------------
    // AnagramApplication.java
    // ------------------------------------------------------------------

    @Test
    @DisplayName("AnagramApplication.java exists at src/main/java/com/example/anagram/")
    void applicationSourceFileExists() {
        Path source = projectRoot().resolve(APPLICATION_SOURCE);
        assertTrue(Files.isRegularFile(source), "expected production source file at " + source);
    }

    @Test
    @DisplayName("AnagramApplication.java declares the package, the annotation and the main method")
    void applicationSourceDeclaresPackageAnnotationAndMain() throws IOException {
        String source = normalizedSource();
        assertTrue(source.startsWith("packagecom.example.anagram;"),
                "AnagramApplication.java must declare 'package com.example.anagram;'");
        assertTrue(source.contains("@SpringBootApplication"),
                "AnagramApplication must be annotated with @SpringBootApplication");
        assertTrue(source.contains("publicclassAnagramApplication"),
                "AnagramApplication must be declared as 'public class AnagramApplication'");
        assertTrue(source.contains("SpringApplication.run(AnagramApplication.class,args);"),
                "main must call SpringApplication.run(AnagramApplication.class, args);");
    }

    @Test
    @DisplayName("AnagramApplication is a public @SpringBootApplication class")
    void applicationClassIsPublicAndAnnotated() {
        assertTrue(Modifier.isPublic(AnagramApplication.class.getModifiers()),
                "AnagramApplication must be a public class");
        assertNotNull(AnagramApplication.class.getAnnotation(SpringBootApplication.class),
                "AnagramApplication must be annotated with @SpringBootApplication");
    }

    @Test
    @DisplayName("AnagramApplication declares public static void main(String[])")
    void applicationClassDeclaresMainMethod() throws Exception {
        Method main = AnagramApplication.class.getMethod("main", String[].class);
        assertTrue(Modifier.isPublic(main.getModifiers()), "main must be public");
        assertTrue(Modifier.isStatic(main.getModifiers()), "main must be static");
        assertEquals(void.class, main.getReturnType(), "main must return void");
    }

    @Test
    @DisplayName("no production class other than AnagramApplication is a Spring Boot application")
    void noOtherProductionClassIsASpringBootApplication() throws IOException {
        Path mainSources = projectRoot().resolve("src/main/java");
        assertTrue(Files.isDirectory(mainSources), "expected production sources at " + mainSources);

        List<Path> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(mainSources)) {
            for (Path file : files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> !"AnagramApplication.java".equals(path.getFileName().toString()))
                    .toList()) {
                if (Files.readString(file).contains("@SpringBootApplication")) {
                    offenders.add(file);
                }
            }
        }
        assertTrue(offenders.isEmpty(),
                "no production class other than AnagramApplication may be annotated with @SpringBootApplication, found: "
                        + offenders);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static Path projectRoot() {
        Path dir = Paths.get("").toAbsolutePath();
        while (dir != null) {
            if (Files.isRegularFile(dir.resolve("pom.xml"))) {
                return dir;
            }
            dir = dir.getParent();
        }
        return Paths.get("").toAbsolutePath();
    }

    private static Document pomDocument() throws Exception {
        Path pom = projectRoot().resolve("pom.xml");
        assertTrue(Files.isRegularFile(pom), "pom.xml must exist at the repository root, expected: " + pom);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        return factory.newDocumentBuilder().parse(pom.toFile());
    }

    private static Element dependenciesElement() throws Exception {
        Element dependencies = child(pomDocument().getDocumentElement(), "dependencies");
        assertNotNull(dependencies, "pom.xml must declare a <dependencies> section");
        return dependencies;
    }

    private static Element findDependency(Element dependencies, String artifactId) {
        for (Element dependency : children(dependencies, "dependency")) {
            if (artifactId.equals(text(dependency, "artifactId"))) {
                return dependency;
            }
        }
        return null;
    }

    private static String normalizedSource() throws IOException {
        Path source = projectRoot().resolve(APPLICATION_SOURCE);
        assertTrue(Files.isRegularFile(source), "expected production source file at " + source);
        return Files.readString(source).replaceAll("\\s+", "");
    }

    private static String localName(Node node) {
        String local = node.getLocalName();
        return local != null ? local : node.getNodeName();
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> result = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && name.equals(localName(node))) {
                result.add((Element) node);
            }
        }
        return result;
    }

    private static Element child(Element parent, String name) {
        List<Element> found = children(parent, name);
        return found.isEmpty() ? null : found.get(0);
    }

    private static String text(Element parent, String name) {
        Element element = child(parent, name);
        return element == null ? null : element.getTextContent().trim();
    }
}
