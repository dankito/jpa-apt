package net.dankito.jpa.apt

import net.dankito.jpa.apt.generated.GeneratedEntityConfigsUtil
import net.dankito.jpa.apt.test_entities.inheritance_annotations.Child_1_1
import net.dankito.jpa.apt.test_entities.inheritance_annotations.MappedSuperclass
import net.dankito.jpa.apt.util.AbstractProcessorTest
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileWriter


class PreviousBuiltGeneratedEntityConfigExistsTest : AbstractProcessorTest() {


    @Before
    override fun setUp() {
        writeMappedSuperclassEntityConfig()
        writePreviousBuiltGeneratedEntityConfigs()
    }


    @Test
    fun testDerivedCorrectlyFromPreviousBuiltGeneratedEntityConfigs() {
        val childClass = Child_1_1::class.java

        val classNames = listOf("inheritance_annotations/${childClass.simpleName}.java")

        process(classNames)
    }


    private fun writeMappedSuperclassEntityConfig() {
        val testEntityClass = MappedSuperclass::class.java

        writeGeneratedFile(testEntityClass.`package`.name, testEntityClass.simpleName + "EntityConfig",
                "package net.dankito.jpa.apt.test_entities.inheritance_annotations;\n" +
                        "\n" +
                        "import java.lang.Exception;\n" +
                        "import java.lang.NoSuchMethodException;\n" +
                        "import java.lang.ReflectiveOperationException;\n" +
                        "import javax.persistence.AccessType;\n" +
                        "import javax.persistence.CascadeType;\n" +
                        "import javax.persistence.FetchType;\n" +
                        "import javax.persistence.GenerationType;\n" +
                        "import net.dankito.jpa.apt.config.ColumnConfig;\n" +
                        "import net.dankito.jpa.apt.config.EntityConfig;\n" +
                        "import net.dankito.jpa.apt.config.Property;\n" +
                        "import net.dankito.jpa.apt.config.RelationType;\n" +
                        "import net.dankito.jpa.apt.reflection.ReflectionHelper;\n" +
                        "\n" +
                        "public final class MappedSuperclassEntityConfig extends EntityConfig {\n" +
                        "  private ReflectionHelper reflectionHelper;\n" +
                        "\n" +
                        "  public MappedSuperclassEntityConfig(EntityConfig parentEntity) throws Exception {\n" +
                        "    super(MappedSuperclass.class);\n" +
                        "\n" +
                        "    reflectionHelper = new ReflectionHelper();\n" +
                        "    reflectionHelper.makeAccessible(this.getConstructor());\n" +
                        "\n" +
                        "    if(parentEntity != null) {\n" +
                        "      parentEntity.addChildEntityConfig(this);\n" +
                        "    }\n" +
                        "\n" +
                        "    this.setTableName(\"MappedSuperclass\");\n" +
                        "    this.setCatalogName(null);\n" +
                        "    this.setSchemaName(null);\n" +
                        "    this.setAccess(AccessType.FIELD);\n" +
                        "\n" +
                        "    addLifeCycleMethods();\n" +
                        "  }\n" +
                        "\n" +
                        "  private ColumnConfig createIdColumnConfig(EntityConfig targetEntity) throws\n" +
                        "      ReflectiveOperationException {\n" +
                        "    ColumnConfig column = new ColumnConfig(this, new Property(this.getEntityClass().getDeclaredField(\"id\"), null, null));\n" +
                        "    reflectionHelper.makeAccessible(column.getProperty());\n" +
                        "\n" +
                        "    column.setColumnName(\"id\");\n" +
                        "    column.setTableName(null);\n" +
                        "    column.setDataType(null);\n" +
                        "\n" +
                        "    column.setId(true);\n" +
                        "    column.setGeneratedId(false);\n" +
                        "    column.setGeneratedIdType(GenerationType.AUTO);\n" +
                        "    column.setIdGenerator(null);\n" +
                        "    column.setGeneratedIdSequence(null);\n" +
                        "\n" +
                        "    column.setVersion(false);\n" +
                        "    column.setLob(false);\n" +
                        "\n" +
                        "    column.setColumnDefinition(null);\n" +
                        "    column.setLength(255);\n" +
                        "    column.setScale(0);\n" +
                        "    column.setPrecision(0);\n" +
                        "\n" +
                        "    column.setCanBeNull(true);\n" +
                        "    column.setUnique(false);\n" +
                        "    column.setInsertable(true);\n" +
                        "    column.setUpdatable(true);\n" +
                        "    column.setFetch(FetchType.EAGER);\n" +
                        "\n" +
                        "    column.setRelationType(RelationType.None);\n" +
                        "\n" +
                        "    column.setTargetEntity(targetEntity);\n" +
                        "\n" +
                        "    column.setOrphanRemoval(false);\n" +
                        "    column.setReferencedColumnName(null);\n" +
                        "\n" +
                        "    column.setJoinColumn(false);\n" +
                        "    column.setCascade(new CascadeType[] {  });\n" +
                        "\n" +
                        "    return column;\n" +
                        "  }\n" +
                        "\n" +
                        "  public void createColumnConfigs() throws Exception {\n" +
                        "    ColumnConfig idColumn = createIdColumnConfig(null);\n" +
                        "    addColumn(idColumn);\n" +
                        "    setIdColumnAndSetItOnChildEntities(idColumn);\n" +
                        "  }\n" +
                        "\n" +
                        "  private void addLifeCycleMethods() throws NoSuchMethodException {\n" +
                        "  }\n" +
                        "}\n"
        )
    }

    private fun writePreviousBuiltGeneratedEntityConfigs() {
        writeGeneratedFile(GeneratedEntityConfigsUtil.GeneratedEntityConfigsPackageName, GeneratedEntityConfigsUtil.GeneratedEntityConfigsClassName,
                "package ${GeneratedEntityConfigsUtil.GeneratedEntityConfigsPackageName};\n" +
                "\n" +
                "import java.lang.Exception;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "import net.dankito.jpa.apt.config.EntityConfig;\n" +
                "import net.dankito.jpa.apt.test_entities.inheritance_annotations.MappedSuperclassEntityConfig;\n" +
                "\n" +
                "public class ${GeneratedEntityConfigsUtil.GeneratedEntityConfigsClassName} extends EntityConfig {\n" +
                "  public List<EntityConfig> getGeneratedEntityConfigs() throws Exception {\n" +
                "    List<EntityConfig> result = new ArrayList<>();\n" +
                "\n" +
                "    MappedSuperclassEntityConfig mappedSuperclassEntityConfig = new MappedSuperclassEntityConfig(null);\n" +
                "    result.add(mappedSuperclassEntityConfig);\n" +
                "\n" +
                "    mappedSuperclassEntityConfig.createColumnConfigs();\n" +
                "\n" +
                "    return result;\n" +
                "  }\n" +
                "}"
        )
    }

    private fun writeGeneratedFile(packageName: String, className: String, fileContent: String) {
        val outputFolder = File(getGeneratedFilesBaseFolder(), packageName.replace('.', '/'))
        outputFolder.mkdirs()

        val outputFile = File(outputFolder, className + ".java")

        val writer = FileWriter(outputFile)

        writer.write(fileContent)

        writer.close()
    }
}