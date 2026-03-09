package com.spendwise.mail.parser;

/**
 * Interface que deben implementar los parsers de mails de cada entidad emisora.
 *
 * Convención de implementación:
 * - Anotar cada implementación con @Component y @Order(N) para controlar prioridad.
 * - canParse() debe ser rápido (solo verificar from/subject, sin regex costosas).
 * - parse() recibe el body en text/plain preferentemente.
 * - Si no se puede determinar la categoría, retornar ParsedExpense con categoryId = null
 *   (el MailImport quedará en estado PENDING para revisión manual).
 */
public interface MailParser {

    /** Nombre de la entidad — se guarda en MailImport.senderEntity */
    String getEntityName();

    /** Retorna true si este parser puede manejar el mail con ese remitente y asunto */
    boolean canParse(String from, String subject);

    /** Extrae los datos de gasto del mail */
    ParsedExpense parse(String subject, String body);

}
