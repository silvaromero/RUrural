## Projeto da disciplina de infraestrutura de hardware

Com o propósito de propocionar uma maior agilidade à entrada dos alunos no R.U.(Restaurante universitário)da UFRPE([Universidade Federal Rural de Pernambuco](http://www.ufrpe.br)), o projeto em questão propõe uma solução que consiste em identificar o aluno e debitar as taxas correspondentes às refeições ofertadas pelo restaurante.

Para viabilizar a construção do projeto, as seguintes tecnologias/recursos foram utilizadas:
  - NodeMCU;
  - Módulo relé;
  - Leitor RFID;
  - TAGs RFID;
  - Display LCD;
  - Protocolo MQTT;
  - C/C++ (Programação do NodeMCU);
  - Java + XML (Cliente android);
  - Python (Serviço web);
  - SQL (Linguagem de consulta do banco de dados).

A solução possui as seguintes funcionalidades:
  - Verificar se o aluno está cadastrado na base de dados;
  - Verificar se o aluno possui saldo suficiente;
  - Cadastrar o aluno na base de dados;
  - Liberar a catraca;
  - Debitar o valor da refeição no saldo atual do aluno;
  - Realizar a recarga de saldo;
  - Consultar o saldo.



> **Docente:** Victor Wanderley Costa de Medeiros