package v1.ajude.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import v1.ajude.comparator.ComparatorCampanhaData;
import v1.ajude.comparator.ComparatorCampanhaLike;
import v1.ajude.comparator.ComparatorCampanhaQuantia;
import v1.ajude.daos.*;
import v1.ajude.models.*;

import java.util.*;

@Service
public class CampanhaServices {

    @Autowired
    private CampanhaRepository<Campanha, Integer> campanhasDAO;
    @Autowired
    private ComentarioRepository<Comentario, Integer> comentariosDAO;
    @Autowired
    private RespostaRepository<Resposta, Integer> respostasDAO;
    @Autowired
    private LikesRepository<Likes, Integer> likesDAO;
    @Autowired
    private DoacaoRepository<Doacao, Integer> doacoesDAO;

    private ComparatorCampanhaLike comparatorCampanhaLike;
    private ComparatorCampanhaData comparatorCampanhaData;
    private ComparatorCampanhaQuantia comparatorCampanhaQuantia;

    @Autowired
    private UsuarioServices usuarioServices;

    public CampanhaServices(CampanhaRepository<Campanha, Integer> campanhasDAO) {
        this.campanhasDAO = campanhasDAO;
        this.comparatorCampanhaLike = new ComparatorCampanhaLike();
        this.comparatorCampanhaData = new ComparatorCampanhaData();
        this.comparatorCampanhaQuantia = new ComparatorCampanhaQuantia();
    }

    public Campanha criarCampanha(String email, Campanha campanha) {
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Usuario usuarioSalvo = recuperaUsuario(email);

        if (campanhaSalva == null && usuarioSalvo != null) {
            Campanha novaCampanha = new Campanha(campanha.getNomeCurto(), campanha.getDescricao(),
                    campanha.getDeadLineString(), campanha.getURL(), campanha.getMeta(), usuarioSalvo);

            usuarioSalvo.setCampanhasCriadas(novaCampanha);

            usuarioServices.addCampanha(novaCampanha);
            campanhasDAO.save(novaCampanha);
            return novaCampanha;
        }
        return null;
    }

    public Optional<Campanha> getCampanha(String url) {
        Optional<Campanha> campanha = campanhasDAO.findByURL(url);
        if (campanha.isPresent()) {
            campanha.get().setStatus(false);
            campanhasDAO.save(campanha.get());
            return campanha;
        }
        return null;
    }

    public List<Campanha> getCampanhas(String subString) {
        List<Campanha> result = new ArrayList<>();
        List<Campanha> campanhas = campanhasDAO.findAll();

        for (Campanha campanha : campanhas) {
            if (campanha.getNomeCurto().toUpperCase().contains(subString.toUpperCase())) {
                result.add(campanha);
            }
        }
        return result;
    }

    public Campanha encerraCampanha(Campanha campanha) {
        Campanha campanhaSalva = recuperaCampanha(campanha);

        if (campanhaSalva != null) {
            campanhaSalva.setStatus(true);
            campanhasDAO.save(campanhaSalva);
            return campanhaSalva;
        }
        return null;
    }

    public Comentario addComentario(Campanha campanha, Comentario comentario, String email) {
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Usuario usuarioSalvo = recuperaUsuario(email);

        if (campanhaSalva != null && usuarioSalvo != null) {
            
            Comentario novoComentario = new Comentario(campanhaSalva, usuarioSalvo, comentario.getTextoComentario());
            campanhaSalva.addComentario(novoComentario);

            comentariosDAO.save(novoComentario);
            campanhasDAO.save(campanhaSalva);
            return novoComentario;
        }
        return null;
    }

    public Comentario apagarComentario(Campanha campanha, Long idComentario, String email) {
        Comentario comentarioSalvo = recuperaComentario(idComentario);
        Campanha campanhaSalva = recuperaCampanha(campanha);

        if (comentarioSalvo != null) {

            comentarioSalvo.setApagado();

            comentariosDAO.save(comentarioSalvo);
            campanhasDAO.save(campanhaSalva);
            return comentarioSalvo;
        }
        return null;
    }

    public Comentario addResposta(Campanha campanha, Long idComentario, Resposta resposta, String email) {
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Usuario usuarioSalvo = recuperaUsuario(email);
        Comentario comentarioSalvo = recuperaComentario(idComentario);

        if (campanhaSalva != null && usuarioSalvo != null && comentarioSalvo != null) {

            Resposta novaResposta = new Resposta(comentarioSalvo, usuarioSalvo, resposta.getTextoResposta());

            comentarioSalvo.addResposta(usuarioSalvo, novaResposta);

            respostasDAO.save(novaResposta);
            comentariosDAO.save(comentarioSalvo);
            campanhasDAO.save(campanhaSalva);
            return comentarioSalvo;
        }
        return null;
    }

    public Comentario apagarResposta(Campanha campanha, Long idComentario, Long idResposta, String email) {
        Comentario comentarioSalvo = recuperaComentario(idComentario);
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Resposta respostaSalva = recuperaResposta(idResposta);

        if (comentarioSalvo != null && respostaSalva != null) {

            respostaSalva.setApagada();

            respostasDAO.save(respostaSalva);
            comentariosDAO.save(comentarioSalvo);
            campanhasDAO.save(campanhaSalva);
            return comentarioSalvo;
        }
        return null;
    }

    public Campanha addLike(Campanha campanha, String email) {
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Usuario usuarioSalvo = recuperaUsuario(email);
        Likes likeSalvo = recuperaLike(campanhaSalva, usuarioSalvo);
        Likes thatLike = null;

        if (campanhaSalva != null && usuarioSalvo != null) {

            if (likeSalvo != null) {
                thatLike = likeSalvo;

                if (thatLike.getLikeMode()) {
                    thatLike.setLikeMode(false);
                } else {
                    thatLike.setLikeMode(true);
                }

            } else {
                thatLike = new Likes(usuarioSalvo, true, campanhaSalva);
            }

            campanhaSalva.setContLike(thatLike);
            likesDAO.save(thatLike);
            campanhasDAO.save(campanhaSalva);
            return campanhaSalva;
        }
        return null;
    }

    public Campanha doarCampanha(Campanha campanha, Doacao doacao, String email) {
        Campanha campanhaSalva = recuperaCampanha(campanha);
        Usuario usuarioSalvo = recuperaUsuario(email);

        if (campanhaSalva != null && usuarioSalvo != null) {

            Doacao novaDoacao = new Doacao(doacao.getQuantia(), doacao.getDataDeDoacaoString(), campanhaSalva, usuarioSalvo);

            campanhaSalva.doarCampanha(novaDoacao);
            usuarioSalvo.setNovaDoacao(novaDoacao);

            doacoesDAO.save(novaDoacao);
            usuarioServices.addDoacao(novaDoacao);
            campanhasDAO.save(campanhaSalva);
            return campanhaSalva;
        }
        return null;
    }

    private Campanha recuperaCampanha(Campanha campanha) {
        Optional<Campanha> campanhaSalva = this.campanhasDAO.findById(campanha.getId());
        if (campanhaSalva.isPresent()) {
            return campanhaSalva.get();
        }
        return null;
    }
    private Campanha recuperaCampanha(Long id) {
        Optional<Campanha> campanhaSalva =  this.campanhasDAO.findById(id);
        if (campanhaSalva.isPresent()) {
            return campanhaSalva.get();
        }
        return null;
    }
    private Usuario recuperaUsuario(String email) {
        Optional<Usuario> usuarioSalvo =  usuarioServices.getUsuario(email);
        if (usuarioSalvo.isPresent()) {
            return usuarioSalvo.get();
        }
        return null;
    }
    private Comentario recuperaComentario(Long idComentario) {
        Optional<Comentario> comentarioSalvo = this.comentariosDAO.findById(idComentario);
        if (comentarioSalvo.isPresent()) {
            return comentarioSalvo.get();
        }
        return null;
    }
    private Likes recuperaLike(Campanha campanha, Usuario usuario) {
        Optional<Likes> like = this.likesDAO.findByUrlAndEmail(campanha.getURL(), usuario.getEmail());
        if (like.isPresent()) {
            return like.get();
        }
        return null;
    }
    private Resposta recuperaResposta(Long idResposta) {
        Optional<Resposta> respostaSalva =  this.respostasDAO.findById(idResposta);
        if (respostaSalva.isPresent()) {
            return respostaSalva.get();
        }
        return null;
    }

    public List<Campanha> getCampanhasLike() {
        List<Campanha> listaCampanhas = this.campanhasDAO.findAll();
        Collections.sort(listaCampanhas, comparatorCampanhaLike);
        return listaCampanhas;
    }
    public List<Campanha> getCampanhasData() {
        List<Campanha> listaCampanhas = this.campanhasDAO.findAll();
        Collections.sort(listaCampanhas, comparatorCampanhaData);
        return listaCampanhas;
    }
    public List<Campanha> getCampanhasQuantia() {
        List<Campanha> listaCampanhas = this.campanhasDAO.findAll();
        Collections.sort(listaCampanhas, comparatorCampanhaQuantia);
        return listaCampanhas;
    }

}
