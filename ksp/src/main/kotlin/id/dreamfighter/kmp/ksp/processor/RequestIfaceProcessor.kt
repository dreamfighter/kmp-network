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

class RequestIfaceProcessor(private val codeGenerator: CodeGenerator, private val logger: KSPLogger) : SymbolProcessor {
    var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val gets = resolver.getSymbolsWithAnnotation(Get::class.qualifiedName.orEmpty())
        val posts = resolver.getSymbolsWithAnnotation(Post::class.qualifiedName.orEmpty())

        val allFiles = gets.plus(posts)

        if (invoked) {
            return emptyList()
        }
        invoked = true

        val visitor = ClassIfaceVisitor(logger)

        codeGenerator.createNewFile(Dependencies(true), "", "NetworkRequest", "kt").use { output ->
            OutputStreamWriter(output).use { writer ->
                if(allFiles.iterator().hasNext()) {
                    val packageName = allFiles.iterator().asSequence().first().containingFile?.packageName?.asString()
                    writer.write("package $packageName\n\n")
                    writer.write("import id.dreamfighter.kmp.network.model.Request\n")
                    writer.write("object Api{\n")
                    allFiles.forEach {
                        logger.warn("processing interface $it")
                        it.accept(visitor, writer)
                    }

                    writer.write("}\n")
                }
            }
        }
        return emptyList()
    }
}

class ClassIfaceVisitor(private val logger: KSPLogger) : KSTopDownVisitor<OutputStreamWriter, Unit>() {
    override fun defaultHandler(node: KSNode, data: OutputStreamWriter) {
    }

    override fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: OutputStreamWriter
    ) {
        super.visitFunctionDeclaration(function, data)
        val symbolName = function.simpleName.asString()
        val dataModels = function.annotations.filter {
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

            function.parameters.forEach { prop ->
                prop.annotations.forEach {
                    when(it.shortName.asString()){
                        Path::class.simpleName -> {
                            logger.info("path ${it.arguments[0].value}")
                            val argName = if(it.arguments[0].value == null){
                                "${prop.name?.asString()}"
                            }else{
                                "${it.arguments[0].value}"
                            }
                            val name = "${prop.name?.asString()}"
                            logger.info("name $name")
                            params[name] = prop.type
                            path[argName] = prop.name?.asString()
                        }
                        Query::class.simpleName -> {
                            logger.info("query ${it.arguments[0]}")
                            val argName = if(it.arguments[0].value == null){
                                "${prop.name?.asString()}"
                            }else{
                                "${it.arguments[0].value}"
                            }
                            val name = "${prop.name?.asString()}"
                            params[name] = prop.type
                            query[argName] = "${prop.name?.asString()}"
                        }
                        Body::class.simpleName -> {
                            val name = "${prop.name?.asString()}"
                            params[name] = prop.type.resolve()
                            body = "${prop.name?.asString()}"
                        }
                        Header::class.simpleName -> {
                            logger.info("header ${it.arguments[0]}")
                            val argName = if(it.arguments[0].value == null){
                                "${prop.name?.asString()}"
                            }else{
                                "${it.arguments[0].value}"
                            }

                            val name = "${prop.name?.asString()}"
                            params[name] = prop.type
                            headerParam[argName] = "${prop.name?.asString()}"
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

class RequestIfaceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return RequestIfaceProcessor(environment.codeGenerator, environment.logger)
    }
}