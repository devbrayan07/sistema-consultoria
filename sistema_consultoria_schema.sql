-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: sistema_consultoria
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `documento`
--

DROP TABLE IF EXISTS `documento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documento` (
  `id_documento` int NOT NULL AUTO_INCREMENT,
  `tipo` enum('NOTA_FISCAL','COMPROVANTE','GUIA','CONTRATO','OUTRO','DECLARACAO') NOT NULL,
  `nome_arquivo` varchar(255) DEFAULT NULL,
  `url_arquivo` varchar(255) DEFAULT NULL,
  `competencia` date DEFAULT NULL,
  `enviado_por` int DEFAULT NULL,
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  `id_empresa` int DEFAULT NULL,
  PRIMARY KEY (`id_documento`),
  UNIQUE KEY `id_documento` (`id_documento`),
  KEY `fk_enviado_documento` (`enviado_por`),
  KEY `idx_documento_empresa_comp` (`id_empresa`,`competencia`),
  CONSTRAINT `fk_empresa_documento` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id_empresa`),
  CONSTRAINT `fk_enviado_documento` FOREIGN KEY (`enviado_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `empresa`
--

DROP TABLE IF EXISTS `empresa`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresa` (
  `id_empresa` int NOT NULL AUTO_INCREMENT,
  `razao_social` varchar(200) DEFAULT NULL,
  `nome_fantasia` varchar(200) DEFAULT NULL,
  `cnpj` varchar(14) DEFAULT NULL,
  `regime_tributario` enum('SIMPLES_NACIONAL','LUCRO_PRESUMIDO','LUCRO_REAL','MEI') DEFAULT NULL,
  `id_usuario_cliente` int DEFAULT NULL,
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_empresa`),
  UNIQUE KEY `id_empresa` (`id_empresa`),
  UNIQUE KEY `cnpj` (`cnpj`),
  KEY `fk_usuario_cliente_id` (`id_usuario_cliente`),
  CONSTRAINT `fk_usuario_cliente_id` FOREIGN KEY (`id_usuario_cliente`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `empresa_responsavel`
--

DROP TABLE IF EXISTS `empresa_responsavel`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `empresa_responsavel` (
  `id_empresa_resp` int NOT NULL AUTO_INCREMENT,
  `id_empresa` int DEFAULT NULL,
  `id_usuario` int DEFAULT NULL,
  PRIMARY KEY (`id_empresa_resp`),
  UNIQUE KEY `id_empresa_resp` (`id_empresa_resp`),
  KEY `fk_empresa_responsavel` (`id_empresa`),
  KEY `fk_usuario_empresa_responsavel` (`id_usuario`),
  CONSTRAINT `fk_empresa_responsavel` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id_empresa`),
  CONSTRAINT `fk_usuario_empresa_responsavel` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notificacao`
--

DROP TABLE IF EXISTS `notificacao`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notificacao` (
  `id_notificacao` int NOT NULL AUTO_INCREMENT,
  `mensagem` varchar(300) DEFAULT NULL,
  `id_usuario` int NOT NULL,
  `lida` tinyint(1) DEFAULT '0',
  `referencia_tipo` enum('OBRIGACAO','DOCUMENTO','RELATORIO') DEFAULT NULL,
  `id_referencia` int DEFAULT NULL,
  `criado_em` datetime DEFAULT NULL,
  PRIMARY KEY (`id_notificacao`),
  UNIQUE KEY `id_notificacao` (`id_notificacao`),
  KEY `idx_notificacao_usuario_lida` (`id_usuario`,`lida`),
  CONSTRAINT `fk_usuario_notificacao` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `obrigacao_fiscal`
--

DROP TABLE IF EXISTS `obrigacao_fiscal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `obrigacao_fiscal` (
  `id_obrigacao_fiscal` int NOT NULL AUTO_INCREMENT,
  `tipo` enum('DAS','DARF','DEFIS','FOLHA','ISS','OUTRO') DEFAULT NULL,
  `competencia` date DEFAULT NULL,
  `data_vencimento` date NOT NULL,
  `id_empresa` int NOT NULL,
  `valor` decimal(12,2) DEFAULT NULL,
  `status` enum('PENDENTE','PAGA','ATRASADA') DEFAULT NULL,
  `data_pagamento` date DEFAULT NULL,
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  `honorario` decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (`id_obrigacao_fiscal`),
  UNIQUE KEY `id_obrigacao_fiscal` (`id_obrigacao_fiscal`),
  KEY `idx_obrigacao_empresa_venc` (`id_empresa`,`data_vencimento`),
  CONSTRAINT `fk_empresa_obrigacao` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id_empresa`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pagamento`
--

DROP TABLE IF EXISTS `pagamento`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pagamento` (
  `id_pagamento` int NOT NULL AUTO_INCREMENT,
  `id` int NOT NULL,
  `id_empresa` int NOT NULL,
  `id_obrigacao_fiscal` int DEFAULT NULL,
  `tipo_pagamento` varchar(30) NOT NULL,
  `metodo_pagamento` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL DEFAULT 'PENDENTE',
  `valor` decimal(12,2) NOT NULL,
  `id_pagamento_externo` varchar(150) DEFAULT NULL,
  `codigo_pix` text,
  `qr_code_pix` longtext,
  `data_criacao` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `data_pagamento` datetime DEFAULT NULL,
  `data_expiracao` datetime DEFAULT NULL,
  PRIMARY KEY (`id_pagamento`),
  KEY `fk_pagamento_usuario` (`id`),
  KEY `fk_pagamento_empresa` (`id_empresa`),
  KEY `fk_pagamento_obrigacao` (`id_obrigacao_fiscal`),
  CONSTRAINT `fk_pagamento_empresa` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id_empresa`),
  CONSTRAINT `fk_pagamento_obrigacao` FOREIGN KEY (`id_obrigacao_fiscal`) REFERENCES `obrigacao_fiscal` (`id_obrigacao_fiscal`),
  CONSTRAINT `fk_pagamento_usuario` FOREIGN KEY (`id`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `relatorio_financeiro`
--

DROP TABLE IF EXISTS `relatorio_financeiro`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `relatorio_financeiro` (
  `id_relatorio_financeiro` int NOT NULL AUTO_INCREMENT,
  `periodo` date DEFAULT NULL,
  `receita` decimal(14,2) DEFAULT NULL,
  `despesa` decimal(14,2) DEFAULT NULL,
  `impostos_pagos` decimal(14,2) DEFAULT NULL,
  `gerado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  `gerado_por` int DEFAULT NULL,
  `id_empresa` int DEFAULT NULL,
  PRIMARY KEY (`id_relatorio_financeiro`),
  UNIQUE KEY `id_relatorio_financeiro` (`id_relatorio_financeiro`),
  KEY `fk_empresa_relatorio` (`id_empresa`),
  KEY `fk_gerado_relatorio` (`gerado_por`),
  CONSTRAINT `fk_empresa_relatorio` FOREIGN KEY (`id_empresa`) REFERENCES `empresa` (`id_empresa`),
  CONSTRAINT `fk_gerado_relatorio` FOREIGN KEY (`gerado_por`) REFERENCES `usuario` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(120) NOT NULL,
  `email` varchar(120) NOT NULL,
  `senha` varchar(255) NOT NULL,
  `tipo` enum('USUARIO','CONTADOR','ADMINISTRADOR') NOT NULL,
  `ativo` tinyint(1) DEFAULT '1',
  `criado_em` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-02 15:38:49
