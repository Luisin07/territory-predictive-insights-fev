package br.com.fiap.territorio.servico;

import br.com.fiap.territorio.dominio.EstrategiaPrevisao;
import br.com.fiap.territorio.dominio.PrevisaoComparacao;
import br.com.fiap.territorio.dominio.PrevisaoMediaMovel;
import br.com.fiap.territorio.excecao.DadosInvalidosException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * PADRÃO FACTORY.
 *
 * Único lugar do sistema que sabe quais estratégias existem e como criá-las.
 * A escolha é feita por um Map (código -> construtor), sem "if (tipo == ...)"
 * nem instanceof. Para criar uma terceira estratégia basta registrar uma
 * linha aqui; o serviço e o menu não mudam.
 */
public class FabricaPrevisao {

    public static final String MEDIA_MOVEL = "MEDIA_MOVEL";
    public static final String COMPARACAO = "COMPARACAO";

    private final Map<String, Supplier<EstrategiaPrevisao>> registro = new LinkedHashMap<>();

    public FabricaPrevisao() {
        registro.put(MEDIA_MOVEL, PrevisaoMediaMovel::new);
        registro.put(COMPARACAO, PrevisaoComparacao::new);
    }

    public EstrategiaPrevisao criar(String codigo) {
        Supplier<EstrategiaPrevisao> construtor = registro.get(codigo == null ? "" : codigo.toUpperCase());
        if (construtor == null) {
            throw new DadosInvalidosException(
                    "Estratégia de previsão desconhecida: '" + codigo + "'. Disponíveis: " + registro.keySet());
        }
        return construtor.get();
    }

    /** Cria as estratégias que estão ativas na configuração. */
    public List<EstrategiaPrevisao> criarAtivas(ConfiguracaoSistema configuracao) {
        return configuracao.getEstrategiasAtivas().stream()
                .map(this::criar)
                .collect(Collectors.toList());
    }
}
