const fs = require('fs');
const path = require('path');

// ============================================================================
// CONFIGURACIÓN
// ============================================================================

const OUTPUT_FILE = 'SII_ERP_AI_COMPLETE_ARCHITECTURE.txt';
const BASE_DIR = path.join(__dirname, '..'); // Un nivel arriba desde /scripts/

// Carpetas a EXCLUIR del análisis
const EXCLUDED_DIRS = [
    'node_modules',
    '.git',
    'dist',
    'build',
    'out',
    'target',
    '.gradle',
    '.idea',
    '.vscode',
    'gradle'
];

// Extensiones de archivos a procesar
const VALID_EXTENSIONS = ['.java', '.xml', '.gradle', '.properties', '.md'];

// ============================================================================
// FUNCIONES AUXILIARES
// ============================================================================

/**
 * Logger simple para consola
 */
const logger = {
    info: (msg) => console.log(msg),
    error: (msg) => console.error(`❌ ${msg}`),
    warn: (msg) => console.warn(`⚠️ ${msg}`)
};

/**
 * Determina si una ruta debe ser ignorada
 */
function shouldIgnore(filePath) {
    return EXCLUDED_DIRS.some(dir => filePath.includes(`${path.sep}${dir}${path.sep}`));
}

/**
 * Obtiene el tipo de archivo basado en su contenido o ubicación
 */
function getFileType(filePath, code) {
    if (filePath.endsWith('.gradle')) return 'Gradle Build Script';
    if (filePath.endsWith('.xml')) return 'XML Configuration';
    if (filePath.endsWith('.properties')) return 'Properties File';
    if (filePath.endsWith('.md')) return 'Documentation';

    // Java specific detection
    if (code.includes('@RestController') || code.includes('@Controller')) return 'Controller';
    if (code.includes('@Service')) return 'Service';
    if (code.includes('@Repository') || code.includes('Repository extends')) return 'Repository';
    if (code.includes('@Entity')) return 'JPA Entity';
    if (code.includes('@Configuration')) return 'Configuration';
    if (code.includes('@Component')) return 'Component';
    if (code.includes('@Test') || filePath.includes('Test.java')) return 'Test';
    if (filePath.includes('domain' + path.sep + 'model')) return 'Domain Model';
    if (filePath.includes('domain' + path.sep + 'valueobject')) return 'Value Object';
    if (filePath.includes('domain' + path.sep + 'event')) return 'Domain Event';
    if (filePath.includes('exception')) return 'Exception';
    if (filePath.includes('dto') || filePath.includes('Request') || filePath.includes('Response')) return 'DTO';
    
    return 'Java Class/Interface';
}

/**
 * Extrae imports de un archivo Java
 */
function extractImports(code) {
    const importRegex = /import\s+([^;]+);/g;
    const imports = new Set();
    let match;
    while ((match = importRegex.exec(code)) !== null) {
        imports.add(match[1].trim());
    }
    return Array.from(imports).sort();
}

/**
 * Extrae definición de clase/interfaz
 */
function extractDefinitions(code) {
    const definitions = [];
    // Regex simple para capturar public class/interface/record/enum Name
    const defRegex = /public\s+(class|interface|record|enum)\s+(\w+)/g;
    let match;
    while ((match = defRegex.exec(code)) !== null) {
        definitions.push(`${match[1]} ${match[2]}`);
    }
    return definitions;
}

/**
 * Detecta issues en el código Java
 */
function detectIssues(code, filePath, type) {
    const issues = [];

    // Java specific checks
    if (code.includes('System.out.println') || code.includes('System.err.println')) {
        issues.push('⚠️ Contiene System.out/err (debería usar Logger/Slf4j)');
    }
    if (code.includes('e.printStackTrace()')) {
        issues.push('⚠️ Contiene e.printStackTrace() (debería usar Logger)');
    }
    if (code.includes('TODO:') || code.includes('TODO ')) {
        issues.push('📝 Contiene TODOs pendientes');
    }
    if (code.includes('FIXME')) {
        issues.push('🔧 Contiene FIXMEs');
    }
    
    // Check for field injection
    if (code.includes('@Autowired') && !code.includes('public ' + path.basename(filePath, '.java'))) {
        // Rough check: has Autowired but maybe no constructor? 
        // Better: check if @Autowired is on a field
        if (code.match(/@Autowired\s+(private|protected|public)/)) {
            issues.push('⚠️ Posible inyección por campo (@Autowired en propiedad). Preferir inyección por constructor.');
        }
    }

    // Check for missing class javadoc
    if (filePath.endsWith('.java') && !code.includes('/**') && !filePath.includes('src' + path.sep + 'test')) {
        issues.push('📄 Posible falta de Javadoc de clase');
    }

    // Empty catch blocks
    if (code.match(/catch\s*\([^)]+\)\s*\{\s*\}/)) {
        issues.push('⚠️ Bloque catch vacío detectado');
    }

    if (code.split('\n').length > 300) {
        issues.push('🔴 Archivo extenso (>300 líneas)');
    }

    return issues;
}

/**
 * Procesa un archivo individual
 */
function processFile(filePath) {
    const fullPath = path.join(BASE_DIR, filePath);
    const relativePath = path.relative(BASE_DIR, fullPath);

    if (!fs.existsSync(fullPath)) return null;

    try {
        const stats = fs.statSync(fullPath);
        if (!stats.isFile()) return null;

        // Intentar leer como texto
        const code = fs.readFileSync(fullPath, 'utf-8');
        const lines = code.split('\n').length;
        const type = getFileType(relativePath, code);
        
        // Solo analizar imports/issues si es Java
        const isJava = relativePath.endsWith('.java');
        const imports = isJava ? extractImports(code) : [];
        const definitions = isJava ? extractDefinitions(code) : [];
        const issues = detectIssues(code, relativePath, type);

        let output = '\n' + '='.repeat(80) + '\n';
        output += `ARCHIVO: ${relativePath}\n`;
        output += `TIPO: ${type}\n`;
        output += `LÍNEAS: ${lines} | TAMAÑO: ${(stats.size / 1024).toFixed(2)} KB\n`;

        if (definitions.length > 0) {
            output += `DEFINICIONES: ${definitions.join(', ')}\n`;
        }

        if (imports.length > 0) {
            output += `\nIMPORTS (${imports.length}):\n`;
            // Mostrar solo los primeros 5 y resumen si hay muchos, o todos si son pocos
            if (imports.length > 10) {
                imports.slice(0, 10).forEach(imp => output += `  - ${imp}\n`);
                output += `  ... y ${imports.length - 10} más\n`;
            } else {
                imports.forEach(imp => output += `  - ${imp}\n`);
            }
        }

        output += `\n${'─'.repeat(80)}\n`;
        output += `CÓDIGO:\n`;
        output += `${'─'.repeat(80)}\n`;
        output += '```java\n';
        output += code;
        output += '\n```\n';

        if (issues.length > 0) {
            output += `\n${'─'.repeat(80)}\n`;
            output += `ANÁLISIS DE CALIDAD:\n`;
            issues.forEach(issue => output += `${issue}\n`);
        }

        output += '='.repeat(80) + '\n';

        return { output, stats: { lines, imports: imports.length, issues, type } };
    } catch (error) {
        return {
            output: `\n⚠️ ERROR AL PROCESAR: ${relativePath}\n   Razón: ${error.message}\n`,
            stats: { lines: 0, imports: 0, issues: [], type: 'Error' }
        };
    }
}

/**
 * Escanea recursivamente un directorio
 */
function scanDirectory(dir, basePath = '') {
    const files = [];
    try {
        const entries = fs.readdirSync(dir, { withFileTypes: true });
        for (const entry of entries) {
            const fullPath = path.join(dir, entry.name);
            const relativePath = path.join(basePath, entry.name);

            if (shouldIgnore(fullPath)) continue;

            if (entry.isDirectory()) {
                files.push(...scanDirectory(fullPath, relativePath));
            } else if (entry.isFile()) {
                const ext = path.extname(entry.name);
                if (VALID_EXTENSIONS.includes(ext)) {
                    files.push(relativePath);
                }
            }
        }
    } catch (error) {
        logger.error(`Error escaneando directorio ${dir}: ${error.message}`);
    }
    return files;
}

/**
 * Categoriza archivos por tipo
 */
function categorizeFiles(fileStats) {
    const categories = {};
    fileStats.forEach(({ file, stats }) => {
        const type = stats.type || 'Other';
        if (!categories[type]) categories[type] = [];
        categories[type].push(file);
    });
    return categories;
}

// ============================================================================
// EJECUCIÓN PRINCIPAL
// ============================================================================

logger.info('🚀 Iniciando análisis de arquitectura SII ERP AI...\n');
logger.info(`📁 Directorio base: ${BASE_DIR}\n`);

// Escanear
logger.info('📂 Escaneando estructura de directorios...');
const allFiles = scanDirectory(BASE_DIR);
logger.info(`✅ Encontrados ${allFiles.length} archivos para analizar\n`);

// Procesar
let fullDoc = '='.repeat(80) + '\n';
fullDoc += 'SII ERP AI - REPORTE DE ARQUITECTURA Y CÓDIGO\n';
fullDoc += `Fecha: ${new Date().toISOString()}\n`;
fullDoc += '='.repeat(80) + '\n\n';

const globalStats = {
    totalFiles: 0,
    totalLines: 0,
    issuesCount: 0,
    filesWithIssues: []
};

const processedFiles = [];

allFiles.forEach(file => {
    logger.info(`   Processing: ${file}`);
    const result = processFile(file);
    if (result) {
        fullDoc += result.output;
        globalStats.totalFiles++;
        globalStats.totalLines += result.stats.lines;
        if (result.stats.issues.length > 0) {
            globalStats.issuesCount += result.stats.issues.length;
            globalStats.filesWithIssues.push({ file, issues: result.stats.issues });
        }
        processedFiles.push({ file, stats: result.stats });
    }
});

// Resumen
const categories = categorizeFiles(processedFiles);

fullDoc += '\n\n' + '='.repeat(80) + '\n';
fullDoc += '🔎 RESUMEN EJECUTIVO\n';
fullDoc += '='.repeat(80) + '\n\n';

fullDoc += `📊 ESTADÍSTICAS:\n`;
fullDoc += `  - Archivos: ${globalStats.totalFiles}\n`;
fullDoc += `  - Líneas de código: ${globalStats.totalLines.toLocaleString()}\n`;
fullDoc += `  - Issues detectados: ${globalStats.issuesCount}\n`;

fullDoc += `\n📂 DISTRIBUCIÓN POR TIPO:\n`;
Object.entries(categories).sort((a, b) => b[1].length - a[1].length).forEach(([type, files]) => {
    fullDoc += `  - ${type}: ${files.length} archivos\n`;
});

if (globalStats.filesWithIssues.length > 0) {
    fullDoc += `\n⚠️ ARCHIVOS CON OBSERVACIONES (${globalStats.filesWithIssues.length}):\n`;
    globalStats.filesWithIssues.forEach(({ file, issues }) => {
        fullDoc += `  - ${file}\n`;
        issues.forEach(i => fullDoc += `    └─ ${i}\n`);
    });
}

// Escribir archivo
const outputPath = path.join(BASE_DIR, OUTPUT_FILE);
fs.writeFileSync(outputPath, fullDoc, 'utf-8');

logger.info(`\n${'='.repeat(80)}`);
logger.info(`✅ ANÁLISIS COMPLETADO`);
logger.info(`📄 Reporte generado en: ${outputPath}`);
logger.info(`${'='.repeat(80)}\n`);
