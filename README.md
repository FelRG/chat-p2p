# Chat P2P

## Descrição do Sistema  
Este trabalho tem como objetivo a criação de um **sistema de chat P2P (peer-to-peer)**, desenvolvido em **Java** utilizando **Swing** para a interface gráfica. O sistema simula um chat interativo onde duas pessoas podem conversar ao mesmo tempo e também inclui a funcionalidade de um grupo geral, permitindo que mensagens sejam enviadas para todos os usuários conectados na mesma rede.

### Funcionalidades do Sistema
- **Chat P2P**: Permite a comunicação entre duas pessoas de forma interativa e simultânea.  
- **Grupo Geral**: Permite o envio de mensagens para todos os usuários conectados na mesma rede.  
- **Interface Gráfica**: Desenvolvida com **Swing**, proporcionando uma interface visual para os usuários interagirem com o sistema.

### Conceitos Abordados  
- **Sockets UDP**: O sistema utiliza **UDP (User Datagram Protocol)** para o envio e recebimento de pacotes de dados.  
  - **Envio de Pacotes**: Mensagens são enviadas de forma rápida e sem confirmação, característica típica do protocolo UDP.  
  - **Recebimento de Pacotes**: Pacotes são recebidos pelos usuários sem a necessidade de estabelecer uma conexão contínua, o que torna a comunicação mais eficiente em termos de latência.

Este trabalho permitiu explorar de maneira prática como os **sockets UDP** podem ser utilizados para estabelecer comunicação em tempo real em um ambiente de rede.
