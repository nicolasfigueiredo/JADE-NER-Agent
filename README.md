# JADE-NER-Agent
Um agente JADE que responde mensagens com as entidades nomeadas que encontrou no seu conteúdo.
Faz uso do modelo em português de Reconhecimento de Entidades Nomeadas desenvolvido para o framework OpenNLP por [Gabriel C. Chiele, Evandro Fonseca e Renata Vieira](http://www.lbd.dcc.ufmg.br/colecoes/eniac/2015/011.pdf) e disponível em http://www.inf.pucrs.br/linatural/Recursos/.

## Exemplo

Mensagem recebida:  "Marque uma reunião com o Pedro às 15 horas, em Brasília."

Resposta enviada: 

"reunião\<event>  
                   Pedro\<person>               
                   15 horas\<time>    
                   Brasília\<place>"

## Usage

Baixe de http://www.inf.pucrs.br/linatural/Recursos/ o zip NER-to-Portuguese.zip, e descompacte a pasta Categorizador_entidades para o diretório de trabalho. Para rodar o JADE criando o agente, o classpath deve conter o caminho para as bibliotecas opennlp-tools, jade.jar e o arquivo Categorizador_entidades/src/Entidade.java. Exemplo:

`java -cp apache-opennlp-1.7.2/lib/opennlp-tools-1.7.2.jar:./:jade/lib/jade.jar jade.Boot -gui -agents foo:NERAgent`
