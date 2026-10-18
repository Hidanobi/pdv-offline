package com.pdv.service;

import com.pdv.model.DocumentoFiscal;
import com.pdv.model.ItemVenda;
import com.pdv.model.Venda;
import com.pdv.repository.DocumentoFiscalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servico de emissao de documentos fiscais.
 *
 * MODOS:
 * - simulacao (padrao): gera XML estruturado conforme layout oficial,
 *   gera chave de acesso e DANFE, mas NAO envia para a SEFAZ.
 *   Ideal para desenvolvimento, testes e ambiente offline.
 *
 * - producao: requer certificado digital A1/A3, integracao com SEFAZ
 *   e assinatura digital. Para habilitar, implemente a chamada ao
 *   WebService da SEFAZ no metodo enviarParaSefaz().
 *
 * DOCUMENTOS:
 * - NFC-e (modelo 65): Nota Fiscal de Consumidor Eletronica
 * - Cupom Fiscal SAT (modelo 59): Cupom Fiscal Eletronico (requer equipamento SAT)
 */
@Service
public class FiscalService {

    @Autowired
    private DocumentoFiscalRepository documentoFiscalRepository;

    @Value("${fiscal.modo:simulacao}")
    private String modoFiscal;

    @Value("${fiscal.empresa.cnpj}")
    private String cnpjEmpresa;

    @Value("${fiscal.empresa.ie}")
    private String ieEmpresa;

    @Value("${fiscal.empresa.nome}")
    private String nomeEmpresa;

    @Value("${fiscal.empresa.fantasia}")
    private String fantasiaEmpresa;

    @Value("${fiscal.empresa.endereco}")
    private String enderecoEmpresa;

    @Value("${fiscal.empresa.cidade}")
    private String cidadeEmpresa;

    @Value("${fiscal.empresa.uf}")
    private String ufEmpresa;

    @Value("${fiscal.empresa.cep}")
    private String cepEmpresa;

    @Value("${fiscal.ambiente:homologacao}")
    private String ambiente;

    private static final int SERIE_NFCE = 1;
    private static final String MODELO_NFCE = "65";
    private static final String MODELO_SAT = "59";
    private static int numeroAtual = 1000;

    /**
     * Emite documento fiscal para a venda.
     * Por padrao emite NFC-e. Pode ser configurado para SAT.
     */
    public DocumentoFiscal emitirDocumentoFiscal(Venda venda) {
        return emitirNFCe(venda);
    }

    /**
     * Emite NFC-e (Nota Fiscal de Consumidor Eletronica) - Modelo 65.
     */
    public DocumentoFiscal emitirNFCe(Venda venda) {
        DocumentoFiscal doc = new DocumentoFiscal();
        doc.setVenda(venda);
        doc.setModelo(MODELO_NFCE);
        doc.setSerie(String.valueOf(SERIE_NFCE));
        doc.setNumero(String.valueOf(++numeroAtual));
        doc.setValorTotal(venda.getTotal());
        doc.setAmbiente(ambiente);
        doc.setDataEmissao(LocalDateTime.now());

        // Calcula ICMS
        BigDecimal valorIcms = calcularIcms(venda);
        doc.setValorIcms(valorIcms);

        // Gera chave de acesso (44 digitos)
        String chave = gerarChaveAcesso(MODELO_NFCE, doc.getNumero());
        doc.setChaveAcesso(chave);

        // Gera XML da NFC-e
        String xml = gerarXmlNFCe(venda, doc);
        doc.setXmlConteudo(xml);

        // Gera DANFE (formato texto para impressao)
        String danfe = gerarDanfeNFCe(venda, doc);
        doc.setDanfeConteudo(danfe);

        // Em modo simulacao, documento e "AUTORIZADO" localmente
        if ("simulacao".equalsIgnoreCase(modoFiscal)) {
            doc.setStatus("SIMULADO");
            doc.setProtocolo("SIM" + System.currentTimeMillis());
            doc.setDataAutorizacao(LocalDateTime.now());
        } else {
            // Modo producao: enviar para SEFAZ (requer certificado digital)
            doc.setStatus("PENDENTE");
            // TODO: Implementar envio para SEFAZ com assinatura digital
            // enviarParaSefaz(doc);
        }

        return documentoFiscalRepository.save(doc);
    }

    /**
     * Emite Cupom Fiscal via SAT (Modelo 59).
     * Requer equipamento SAT conectado. Em modo simulacao, gera o cupom.
     */
    public DocumentoFiscal emitirCupomSAT(Venda venda) {
        DocumentoFiscal doc = new DocumentoFiscal();
        doc.setVenda(venda);
        doc.setModelo(MODELO_SAT);
        doc.setSerie("1");
        doc.setNumero(String.valueOf(++numeroAtual));
        doc.setValorTotal(venda.getTotal());
        doc.setAmbiente(ambiente);
        doc.setDataEmissao(LocalDateTime.now());

        BigDecimal valorIcms = calcularIcms(venda);
        doc.setValorIcms(valorIcms);

        String chave = gerarChaveAcesso(MODELO_SAT, doc.getNumero());
        doc.setChaveAcesso(chave);

        String xml = gerarXmlCFe(venda, doc);
        doc.setXmlConteudo(xml);

        String cupom = gerarCupomFiscal(venda, doc);
        doc.setDanfeConteudo(cupom);

        if ("simulacao".equalsIgnoreCase(modoFiscal)) {
            doc.setStatus("SIMULADO");
            doc.setProtocolo("SAT" + System.currentTimeMillis());
            doc.setDataAutorizacao(LocalDateTime.now());
        } else {
            doc.setStatus("PENDENTE");
            // TODO: Enviar para equipamento SAT via driver
            // enviarParaSat(doc);
        }

        return documentoFiscalRepository.save(doc);
    }

    /**
     * Gera chave de acesso de 44 digitos conforme layout oficial.
     * CFeSAT: 22 digitos; NFC-e: 44 digitos.
     */
    private String gerarChaveAcesso(String modelo, String numero) {
        // cUF(2) + AAMM(4) + CNPJ(14) + modelo(2) + serie(3) + numero(9) + tpEmissao(1) + codigo(8) + DV(1)
        String cUF = "35"; // SP
        String aamm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String serie = String.format("%03d", SERIE_NFCE);
        String nNF = String.format("%09d", Long.parseLong(numero));
        String tpEmissao = "1";
        String codigo = String.format("%08d", System.currentTimeMillis() % 100000000);
        String base = cUF + aamm + cnpjEmpresa + modelo + serie + nNF + tpEmissao + codigo;
        int dv = calcularDigitoVerificador(base);
        return base + dv;
    }

    private int calcularDigitoVerificador(String base) {
        int peso = 2;
        int soma = 0;
        for (int i = base.length() - 1; i >= 0; i--) {
            soma += Character.getNumericValue(base.charAt(i)) * peso;
            peso = (peso == 9) ? 2 : peso + 1;
        }
        int resto = soma % 11;
        return (resto == 0 || resto == 1) ? 0 : 11 - resto;
    }

    private BigDecimal calcularIcms(Venda venda) {
        BigDecimal totalIcms = BigDecimal.ZERO;
        for (ItemVenda item : venda.getItens()) {
            BigDecimal aliquota = item.getAliquotaIcms() != null ?
                    item.getAliquotaIcms() : BigDecimal.ZERO;
            BigDecimal icmsItem = item.getValorTotal()
                    .multiply(aliquota)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalIcms = totalIcms.add(icmsItem);
        }
        return totalIcms;
    }

    /**
     * Gera XML da NFC-e conforme layout oficial (NFe 4.00).
     */
    private String gerarXmlNFCe(Venda venda, DocumentoFiscal doc) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<NFe xmlns=\"http://www.portalfiscal.inf.br/nfe\">\n");
        xml.append("  <infNFe Id=\"NFe").append(doc.getChaveAcesso()).append("\" versao=\"4.00\">\n");
        xml.append("    <ide>\n");
        xml.append("      <cUF>35</cUF>\n");
        xml.append("      <natOp>Venda ao consumidor</natOp>\n");
        xml.append("      <mod>").append(doc.getModelo()).append("</mod>\n");
        xml.append("      <serie>").append(doc.getSerie()).append("</serie>\n");
        xml.append("      <nNF>").append(doc.getNumero()).append("</nNF>\n");
        xml.append("      <dhEmi>").append(doc.getDataEmissao().toString()).append("</dhEmi>\n");
        xml.append("      <tpNF>1</tpNF>\n");
        xml.append("      <cMunFG>3550308</cMunFG>\n");
        xml.append("      <tpImp>4</tpImp>\n");
        xml.append("      <tpEmis>1</tpEmis>\n");
        xml.append("      <cDV>").append(doc.getChaveAcesso().charAt(43)).append("</cDV>\n");
        xml.append("      <tpAmb>").append("homologacao".equals(ambiente) ? 2 : 1).append("</tpAmb>\n");
        xml.append("    </ide>\n");
        xml.append("    <emit>\n");
        xml.append("      <CNPJ>").append(cnpjEmpresa).append("</CNPJ>\n");
        xml.append("      <xNome>").append(nomeEmpresa).append("</xNome>\n");
        xml.append("      <xFant>").append(fantasiaEmpresa).append("</xFant>\n");
        xml.append("      <enderEmit>\n");
        xml.append("        <xLgr>").append(enderecoEmpresa).append("</xLgr>\n");
        xml.append("        <xMun>").append(cidadeEmpresa).append("</xMun>\n");
        xml.append("        <UF>").append(ufEmpresa).append("</UF>\n");
        xml.append("        <CEP>").append(cepEmpresa).append("</CEP>\n");
        xml.append("      </enderEmit>\n");
        xml.append("      <IE>").append(ieEmpresa).append("</IE>\n");
        xml.append("      <CRT>3</CRT>\n");
        xml.append("    </emit>\n");

        if (venda.getCpfCliente() != null && !venda.getCpfCliente().isEmpty()) {
            xml.append("    <dest>\n");
            xml.append("      <CPF>").append(venda.getCpfCliente()).append("</CPF>\n");
            xml.append("      <xNome>").append(venda.getNomeCliente() != null ? venda.getNomeCliente() : "").append("</xNome>\n");
            xml.append("    </dest>\n");
        }

        xml.append("    <det>\n");
        int nItem = 1;
        for (ItemVenda item : venda.getItens()) {
            xml.append("      <item n=\"").append(nItem++).append("\">\n");
            xml.append("        <prod>\n");
            xml.append("          <cProd>").append(item.getProduto().getId()).append("</cProd>\n");
            xml.append("          <cEAN>").append(item.getCodigoBarras()).append("</cEAN>\n");
            xml.append("          <xProd>").append(item.getDescricao()).append("</xProd>\n");
            xml.append("          <NCM>").append(item.getNcm() != null ? item.getNcm() : "0000").append("</NCM>\n");
            xml.append("          <uCom>").append(item.getProduto().getUnidade()).append("</uCom>\n");
            xml.append("          <qCom>").append(item.getQuantidade()).append("</qCom>\n");
            xml.append("          <vUnCom>").append(item.getPrecoUnitario()).append("</vUnCom>\n");
            xml.append("          <vProd>").append(item.getValorTotal()).append("</vProd>\n");
            xml.append("        </prod>\n");
            xml.append("        <imposto>\n");
            xml.append("          <ICMS>\n");
            xml.append("            <ICMSSN102>\n");
            xml.append("              <orig>0</orig>\n");
            xml.append("              <CSOSN>102</CSOSN>\n");
            xml.append("            </ICMSSN102>\n");
            xml.append("          </ICMS>\n");
            xml.append("        </imposto>\n");
            xml.append("      </item>\n");
        }
        xml.append("    </det>\n");
        xml.append("    <total>\n");
        xml.append("      <vNF>").append(venda.getTotal()).append("</vNF>\n");
        xml.append("    </total>\n");
        xml.append("  </infNFe>\n");
        xml.append("</NFe>");
        return xml.toString();
    }

    /**
     * Gera XML do CFe (Cupom Fiscal Eletronico) - SAT.
     */
    private String gerarXmlCFe(Venda venda, DocumentoFiscal doc) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<CFe>\n");
        xml.append("  <infCFe Id=\"CFe").append(doc.getChaveAcesso(), 0, Math.min(40, doc.getChaveAcesso().length())).append("\" versao=\"0.07\">\n");
        xml.append("    <ide>\n");
        xml.append("      <CNPJ>").append(cnpjEmpresa).append("</CNPJ>\n");
        xml.append("      <signAC>SIMULADO</signAC>\n");
        xml.append("      <numeroCaixa>001</numeroCaixa>\n");
        xml.append("      <dEmi>").append(doc.getDataEmissao().format(DateTimeFormatter.ofPattern("yyyyMMdd"))).append("</dEmi>\n");
        xml.append("      <hEmi>").append(doc.getDataEmissao().format(DateTimeFormatter.ofPattern("HHmmss"))).append("</hEmi>\n");
        xml.append("    </ide>\n");
        xml.append("    <emit>\n");
        xml.append("      <CNPJ>").append(cnpjEmpresa).append("</CNPJ>\n");
        xml.append("      <IE>").append(ieEmpresa).append("</IE>\n");
        xml.append("      <xNome>").append(nomeEmpresa).append("</xNome>\n");
        xml.append("      <xFant>").append(fantasiaEmpresa).append("</xFant>\n");
        xml.append("    </emit>\n");
        xml.append("    <dest>\n");
        xml.append("      <CPF>").append(venda.getCpfCliente() != null ? venda.getCpfCliente() : "").append("</CPF>\n");
        xml.append("      <xNome>").append(venda.getNomeCliente() != null ? venda.getNomeCliente() : "").append("</xNome>\n");
        xml.append("    </dest>\n");
        xml.append("    <det>\n");
        int nItem = 1;
        for (ItemVenda item : venda.getItens()) {
            xml.append("      <item nItem=\"").append(nItem++).append("\">\n");
            xml.append("        <prod>\n");
            xml.append("          <cProd>").append(item.getProduto().getId()).append("</cProd>\n");
            xml.append("          <cEAN>").append(item.getCodigoBarras()).append("</cEAN>\n");
            xml.append("          <xProd>").append(item.getDescricao()).append("</xProd>\n");
            xml.append("          <NCM>").append(item.getNcm() != null ? item.getNcm() : "0000").append("</NCM>\n");
            xml.append("          <uCom>").append(item.getProduto().getUnidade()).append("</uCom>\n");
            xml.append("          <qCom>").append(item.getQuantidade()).append("</qCom>\n");
            xml.append("          <vUnCom>").append(item.getPrecoUnitario()).append("</vUnCom>\n");
            xml.append("          <vProd>").append(item.getValorTotal()).append("</vProd>\n");
            xml.append("        </prod>\n");
            xml.append("      </item>\n");
        }
        xml.append("    </det>\n");
        xml.append("    <total>\n");
        xml.append("      <vCFe>").append(venda.getTotal()).append("</vCFe>\n");
        xml.append("    </total>\n");
        xml.append("  </infCFe>\n");
        xml.append("</CFe>");
        return xml.toString();
    }

    /**
     * Gera DANFE da NFC-e em formato texto para impressao/visualizacao.
     */
    private String gerarDanfeNFCe(Venda venda, DocumentoFiscal doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append("        NOTA FISCAL DE CONSUMIDOR ELETRONICA\n");
        sb.append("                 (NFC-e) MODELO 65\n");
        sb.append("=================================================\n");
        sb.append(nomeEmpresa).append("\n");
        sb.append("CNPJ: ").append(formatarCnpj(cnpjEmpresa)).append("  IE: ").append(ieEmpresa).append("\n");
        sb.append(enderecoEmpresa).append(" - ").append(cidadeEmpresa).append("/").append(ufEmpresa).append("\n");
        sb.append("CEP: ").append(cepEmpresa).append("\n");
        sb.append("-------------------------------------------------\n");
        sb.append("Numero: ").append(doc.getNumero()).append("  Serie: ").append(doc.getSerie()).append("\n");
        sb.append("Data/Hora: ").append(doc.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Protocolo: ").append(doc.getProtocolo()).append("\n");
        sb.append("Chave de Acesso:\n").append(doc.getChaveAcesso()).append("\n");
        if (venda.getCpfCliente() != null && !venda.getCpfCliente().isEmpty()) {
            sb.append("CPF: ").append(formatarCpf(venda.getCpfCliente())).append("\n");
            sb.append("Nome: ").append(venda.getNomeCliente() != null ? venda.getNomeCliente() : "").append("\n");
        }
        sb.append("-------------------------------------------------\n");
        sb.append(String.format("%-6s %-25s %6s %8s %8s\n", "COD", "DESCRICAO", "QTD", "VAL.UN", "VAL.TOT"));
        sb.append("-------------------------------------------------\n");
        for (ItemVenda item : venda.getItens()) {
            sb.append(String.format("%-6s %-25s %6s %8s %8s\n",
                    item.getProduto().getId(),
                    truncate(item.getDescricao(), 25),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    item.getValorTotal()));
        }
        sb.append("-------------------------------------------------\n");
        sb.append(String.format("%-40s %8s\n", "SUBTOTAL", venda.getSubtotal()));
        if (venda.getDesconto() != null && venda.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-40s %8s\n", "DESCONTO", venda.getDesconto()));
        }
        sb.append(String.format("%-40s %8s\n", "TOTAL", venda.getTotal()));
        sb.append("-------------------------------------------------\n");
        sb.append("Pagamento: ").append(venda.getFormaPagamento()).append("\n");
        sb.append("Valor Pago: ").append(venda.getValorPago()).append("\n");
        sb.append("Troco: ").append(venda.getTroco()).append("\n");
        sb.append("-------------------------------------------------\n");
        sb.append("ICMS: ").append(doc.getValorIcms()).append("\n");
        sb.append("Ambiente: ").append(ambiente.toUpperCase()).append("\n");
        sb.append("Documento emitido em MODO SIMULACAO\n");
        sb.append("Consulte em: www.fazenda.sp.gov.br/nfce\n");
        sb.append("=================================================\n");
        return sb.toString();
    }

    /**
     * Gera Cupom Fiscal (SAT) em formato texto para impressao.
     */
    private String gerarCupomFiscal(Venda venda, DocumentoFiscal doc) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================================\n");
        sb.append("           CUPOM FISCAL ELETRONICO\n");
        sb.append("                  (SAT) MODELO 59\n");
        sb.append("=================================================\n");
        sb.append(nomeEmpresa).append("\n");
        sb.append("CNPJ: ").append(formatarCnpj(cnpjEmpresa)).append("\n");
        sb.append("IE: ").append(ieEmpresa).append("\n");
        sb.append(enderecoEmpresa).append("\n");
        sb.append(cidadeEmpresa).append("/").append(ufEmpresa).append(" - CEP ").append(cepEmpresa).append("\n");
        sb.append("-------------------------------------------------\n");
        sb.append("Cupom: ").append(doc.getNumero()).append("  Serie: ").append(doc.getSerie()).append("\n");
        sb.append("Data: ").append(doc.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))).append("\n");
        sb.append("Assinatura SAT: SIMULADO\n");
        sb.append("-------------------------------------------------\n");
        for (ItemVenda item : venda.getItens()) {
            sb.append(String.format("%-6s %-25s %6s x %7s = %8s\n",
                    item.getProduto().getId(),
                    truncate(item.getDescricao(), 25),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    item.getValorTotal()));
        }
        sb.append("-------------------------------------------------\n");
        sb.append(String.format("%-40s %8s\n", "TOTAL GERAL", venda.getTotal()));
        sb.append("-------------------------------------------------\n");
        sb.append("Forma Pagto: ").append(venda.getFormaPagamento()).append("\n");
        sb.append("Valor Recebido: ").append(venda.getValorPago()).append("\n");
        sb.append("Troco: ").append(venda.getTroco()).append("\n");
        sb.append("-------------------------------------------------\n");
        sb.append("Fonte: SAT - AMBIENTE ").append(ambiente.toUpperCase()).append("\n");
        sb.append("Cupom emitido em MODO SIMULACAO\n");
        sb.append("=================================================\n");
        return sb.toString();
    }

    private String formatarCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) return cnpj;
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + "." +
                cnpj.substring(5, 8) + "/" + cnpj.substring(8, 12) + "-" + cnpj.substring(12);
    }

    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" + cpf.substring(9);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }

    public List<DocumentoFiscal> listarTodos() {
        return documentoFiscalRepository.findAll();
    }

    public Optional<DocumentoFiscal> buscarPorId(Long id) {
        return documentoFiscalRepository.findById(id);
    }

    public Optional<DocumentoFiscal> buscarPorVenda(Long vendaId) {
        return documentoFiscalRepository.findByVendaId(vendaId);
    }

    public String getModoFiscal() {
        return modoFiscal;
    }
}
