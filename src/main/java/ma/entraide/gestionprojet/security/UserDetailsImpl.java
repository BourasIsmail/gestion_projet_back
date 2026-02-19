package ma.entraide.gestionprojet.security;

import ma.entraide.gestionprojet.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    private final Long id;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String password;
    private final boolean actif;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserDetailsImpl fromUser(User user) {
        return new UserDetailsImpl(user);
    }

    public UserDetailsImpl(User user) {
        this.id = user.getId();
        this.nom = user.getNom();
        this.prenom = user.getPrenom();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.actif = user.getActif();
        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRoleGlobal().name())
        );
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getEmail() { return email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return actif; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return actif; }

    public String getNomComplet() { return prenom + " " + nom; }
}


