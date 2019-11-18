package v1.ajude.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import javax.persistence.*;

@MappedSuperclass
@Getter
@ToString
public abstract class ComentarioAbstract {

    @ManyToOne
    @JoinColumn(name = "email")
    @JsonIgnore
    private Usuario usuario;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long idComent;
    private String comentario;

    public ComentarioAbstract() {
        super();
    }

    public ComentarioAbstract(Usuario usuario, String comentario) {
        this.usuario = usuario;
        this.comentario = comentario;
    }

    public long getIdComent() {
        return this.idComent;
    }

    public String getComentario() {
        return this.comentario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

}
