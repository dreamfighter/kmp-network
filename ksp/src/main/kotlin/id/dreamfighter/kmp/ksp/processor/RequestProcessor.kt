package id.dreamfighter.kmp.ksp.processor

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import id.dreamfighter.multiplatform.annotation.Body
import id.dreamfighter.multiplatform.annotation.Get
import id.dreamfighter.multiplatform.annotation.Header
import id.dreamfighter.multiplatform.annotation.Multipart
import id.dreamfighter.multiplatform.annotation.Path
import id.dreamfighter.multiplatform.annotation.Post
import id.dreamfighter.multiplatform.annotation.Query
import java.io.OutputStreamWriter

class RequestProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val gets = resolver.getSymbolsWithAnnotation(Get::class.qualifiedName.orEmpty())
        val posts = resolver.getSymbolsWithAnnotation(Post::class.qualifiedName.orEmpty())

        val allFiles = gets.plus(posts)

        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = ClassVisitor(logger)

        codeGenerator.createNewFile(Dependencies(true), "", "ApiRequest", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                if(allFiles.iterator().hasNext()) {
                    val packageName = allFiles.iterator().asSequence().first().containingFile?.packageName?.asString()
                    writer.write("package $packageName\n\n")
                    writer.write("import id.dreamfighter.kmp.network.model.Request\n")
                    writer.write("object Req{\n")
                    allFiles.forEach {
                        logger.warn("processing $it")
                        it.accept(visitor, writer)
                    }

                    writer.write("}\n")
                }
            }
        }
        return emptyList()
    }
}

class ClassVisitor(private val logger: KSPLogger) : KSTopDownVisitor<OutputStreamWriter, Unit>() {
    override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: OutputStreamWriter
    ) {
        super.visitClassDeclaration(classDeclaration, data)
        val symbolName = classDeclaration.simpleName.asString()
        val dataModels = classDeclaration.annotations.filter {
            it.shortName.asString() == Get::class.simpleName || it.shortName.asString() == Post::class.simpleName || it.shortName.asString() == Multipart::class.simpleName
        }
        if(dataModels.iterator().hasNext()){
            var method = ""
            var url = ""
            val path: MutableMap<String, Any?> = mutableMapOf()
            val query:MutableMap<String,Any?> = mutableMapOf()
            val headerParam:MutableMap<String,Any?> = mutableMapOf()
            val params:MutableMap<String,Any?> = mutableMapOf()
            var body = "null"
            val headers:MutableMap<String,Any?> = mutableMapOf()
            val multipart = dataModels.filter {
                it.shortName.asString() == Multipart::class.simpleName
            }
            val annotation = dataModels.filter { it.shortName.asString() == Get::class.simpleName || it.shortName.asString() == Post::class.simpleName }.first()

            logger.warn("${annotation.arguments.first().value}")
            if(!multipart.none()) {
                logger.warn("$symbolName is multipart")
            }

            when(annotation.shortName.asString()){
                Get::class.simpleName -> {
                    method = "GET"
                    url = annotation.arguments.first().value as String
                }
                Post::class.simpleName -> {
                    method = "POST"
                    val arguments = annotation.arguments
                    if(!multipart.none()){
                        headers.plusAssign(
                            "Content-Type" to "\"${(arguments.first { it.name?.asString() == "contentType" }.value ?: "mutlipart/form-data")}\""
                        )
                    }else {
                        headers.plusAssign(
                            "Content-Type" to "\"${(arguments.first { it.name?.asString() == "contentType" }.value ?: "application/json")}\""
                        )
                    }
                    url = arguments.first { it.name?.asString() == "url" }.value as String
                }
            }

            classDeclaration.getDeclaredProperties().forEach { prop ->
                prop.annotations.forEach {
                    when(it.shortName.asString()){
                        Path::class.simpleName -> {
                            logger.info("path ${it.arguments[0].value}")
                            val argName = if(it.arguments[0].value == null){
                                prop.simpleName.asString()
                            }else{
                                "${it.arguments[0].value}"
                            }
                            val name = prop.simpleName.asString()
                            logger.info("name $name")
                            params[name] = prop.type
                            path[argName] = prop.simpleName.asString()
                        }
                        Query::class.simpleName -> {
                            logger.info("query ${it.arguments[0]}")
                            val argName = if(it.arguments[0].value == null){
                                prop.simpleName.asString()
                            }else{
                                "${it.arguments[0].value}"
                            }
                            val name = prop.simpleName.asString()
                            params[name] = prop.type
                            query[argName] = prop.simpleName.asString()
                        }
                        Body::class.simpleName -> {
                            val name = prop.simpleName.asString()
                            params[name] = prop.type.resolve()
                            body = prop.simpleName.asString()
                        }
                        Header::class.simpleName -> {
                            logger.info("header ${it.arguments[0]}")
                            val argName = if(it.arguments[0].value == null){
                                prop.simpleName.asString()
                            }else{
                                "${it.arguments[0].value}"
                            }

                            val name = prop.simpleName.asString()
                            params[name] = prop.type
                            headerParam[argName] = prop.simpleName.asString()
                        }
                    }
                }
            }

            val paramsString = params.map {
                "${it.key}:${it.value}"
            }.joinToString(",")

            headers.plusAssign(headerParam)

            data.write("""
    fun $symbolName($paramsString):Request {
        return Request(url = "$url", method = "$method", path = mapOf(${path.map {
                "\"${it.value}\" to ${it.value}"
            }.joinToString(",")}),query = mapOf(${query.map {
                "\"${it.value}\" to ${it.value}"
            }.joinToString(",")}), body = $body, requestHeaders = mapOf(${headers.map {
                "\"${it.key}\" to ${it.value}"
            }.joinToString(",")}))
    }

""".trimMargin())
        }

    }
}

class RequestProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RequestProcessor(environment.codeGenerator, environment.logger)
    }
}