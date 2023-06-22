package at.kaindorf.taschengeldverwaltung.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VillagerPerson extends Person {

    private String shortSign;
    private LocalDate dateOfBirth;
    private LocalDate dateOfExit;
    private String note;
    private Person trustedPerson;

    public VillagerPerson(@NonNull Long id, String firstName, String lastName, String titleBefore, String titleAfter, Salutation salutation, String shortSign, LocalDate dateOfBirth, LocalDate dateOfExit, String note, Person trustedPerson) {
        super(id, firstName, lastName, titleBefore, titleAfter, salutation);
        this.shortSign = shortSign;
        this.dateOfBirth = dateOfBirth;
        this.dateOfExit = dateOfExit;
        this.note = note;
        this.trustedPerson = trustedPerson;
    }
}
