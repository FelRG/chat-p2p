package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Mensagem;
import br.ufsm.poli.csi.redes.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UDPServiceImpl implements UDPService {

    private Usuario meuUsuario;
    private Map<Usuario, Usuario> usuarios = new HashMap<>();

    private class RecebeUDP implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            DatagramSocket socket = new DatagramSocket(8080);
            while (true) {
                DatagramPacket pacoteUDP = new DatagramPacket(new byte[1024], 1024);
                socket.receive(pacoteUDP);
                String strPacote = new String(pacoteUDP.getData(),
                        0,
                        pacoteUDP.getLength(),
                        "UTF-8");
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Mensagem mensagem = mapper.readValue(strPacote, Mensagem.class);
                    Usuario usuario = new Usuario(mensagem.getUsuario(),
                            pacoteUDP.getAddress(),
                            Usuario.StatusUsuario.valueOf(mensagem.getStatus()),
                            new Date());


                    // Verifica se o usuário está novo ou já existe
                    if (!usuarios.containsKey(usuario)) {
                        usuarios.put(usuario, usuario);
                        if (usuarioListener != null) {
                            usuarioListener.usuarioAdicionado(usuario);
                        }
                    } else {
                        // Verifica se o status do usuário foi alterado
                        Usuario usuarioExistente = usuarios.get(usuario);
                        if (!usuarioExistente.getStatus().equals(usuario.getStatus())) {
                            usuarios.put(usuario, usuario);
                            if (usuarioListener != null) {
                                usuarioListener.usuarioAlterado(usuario);
                            }
                        }
                    }
                    

                    // Lógica para verificar se o usuário foi removido
                    // Por exemplo, se a mensagem indicar que o usuário saiu ou foi desconectado
                    /*if (mensagem.getTipoMensagem().equals("sair")) {
                        usuarios.remove(usuario);
                        if (usuarioListener != null) {
                            usuarioListener.usuarioRemovido(usuario);
                        }
                    }*/

                    //MODIFICACAO A SER ANALISADA
                    // Notifica o listener de mensagem recebida
                    if (mensagemListener != null && mensagem.getTipoMensagem().equals("mensagem")) {
                        mensagemListener.mensagemRecebida(mensagem.getMsg(), usuario, false);
                    }

                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class EnviaMensagemUDP implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                if (meuUsuario != null && meuUsuario.getNome() != null) {
                    DatagramSocket socketUDP = new DatagramSocket();
                    String ip = "192.168.0.";
                    for (int i = 1; i < 255; i++) {
                        Mensagem mensagem = new Mensagem("sonda", meuUsuario.getNome(),
                                meuUsuario.getStatus().toString(), null);
                        String strPacote = new ObjectMapper().writeValueAsString(mensagem);
                        byte[] bytes = strPacote.getBytes();
                        DatagramPacket pacoteUDP =
                                new DatagramPacket(bytes,
                                        bytes.length,
                                        InetAddress.getByName(ip + i),
                                        8080);
                        socketUDP.send(pacoteUDP);
                    }
                }
                Thread.sleep(5000);
            }
        }
    }

    /*@Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
        System.out.println("[ENVIA MENSAGEM] " + mensagem + " --> " + destinatario.getNome() + "/" +
                destinatario.getEndereco().getHostAddress()
        );
    }*/

    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {
        if (destinatario.getEndereco() != null) { // USUÁRIO ESPECÍFICO
            try (DatagramSocket socketUDP = new DatagramSocket()) {
                // Verificar se o destinatário existe no mapa de usuários
                if (!usuarios.containsKey(destinatario)) {
                    System.out.println("[AVISO] Usuário " + destinatario.getNome() + " está indisponível.");
                    return;
                }

                // Criar e enviar a mensagem
                Mensagem msg = new Mensagem("mensagem", meuUsuario.getNome(), meuUsuario.getStatus().toString(), mensagem);
                String strPacote = new ObjectMapper().writeValueAsString(msg);
                byte[] bytes = strPacote.getBytes();

                DatagramPacket pacoteUDP = new DatagramPacket(
                        bytes,
                        bytes.length,
                        destinatario.getEndereco(),
                        8080
                );

                socketUDP.send(pacoteUDP);
                System.out.println("[ENVIA MENSAGEM] " + mensagem + " --> " + destinatario.getNome() + " (" + destinatario.getEndereco().getHostAddress() + ")");

            } catch (Exception e) {
                System.err.println("[ERRO] Falha ao enviar mensagem para " + destinatario.getNome() + ": " + e.getMessage());
            }
        } else {
            if(Objects.equals(destinatario.getNome(), "Geral")){ // USUÁRIO GERAL - CHAT GERAL

                try (DatagramSocket socketUDP = new DatagramSocket()) {
                    // Envia a mensagem para todos os usuários no chat geral
                    Mensagem msg = new Mensagem("mensagem", meuUsuario.getNome(), meuUsuario.getStatus().toString(), mensagem);
                    String strPacote = new ObjectMapper().writeValueAsString(msg);
                    byte[] bytes = strPacote.getBytes();


                        // Evita enviar a mensagem de volta ao próprio remetente e verifica se o usuário tem endereço
                        /*if ((meuUsuario != destinatario) && (destinatario.getEndereco() == null)) {
                            DatagramPacket pacoteUDP = new DatagramPacket(bytes, bytes.length, destinatario.getEndereco(), 8080);
                            socketUDP.send(pacoteUDP);
                            System.out.println("[ENVIA MENSAGEM - GERAL] " + mensagem + " --> " + destinatario.getNome()
                                    + " (" + destinatario.getEndereco().getHostAddress() + ")");
                        }*/

                    for (Usuario usuario : usuarios.values()) {
                        if (!usuario.equals(meuUsuario) && usuario.getEndereco() != null) { // Evita enviar para si mesmo
                            DatagramPacket pacoteUDP = new DatagramPacket(bytes, bytes.length, usuario.getEndereco(), 8080);
                            socketUDP.send(pacoteUDP);
                        }
                    }

                } catch (Exception e) {
                    System.err.println("[ERRO] Falha ao enviar mensagem para o chat geral: " + e.getMessage());
                }

            } else{
                System.out.println("[AVISO] Usuário " + destinatario.getNome() + " não possui um endereço associado.");
            }
        }
    }




    @Override
    public void usuarioAlterado(Usuario usuario) {
        this.meuUsuario = usuario;
    }

    private UDPServiceMensagemListener mensagemListener;
    @Override
    public void addListenerMensagem(UDPServiceMensagemListener listener) {
        this.mensagemListener = listener;
    }

    private UDPServiceUsuarioListener usuarioListener;

    @Override
    public void addListenerUsuario(UDPServiceUsuarioListener listener) {
        this.usuarioListener = listener;
    }

    public UDPServiceImpl() {
        new Thread(new EnviaMensagemUDP()).start();
        new Thread(new RecebeUDP()).start();
}
}