package pt.ulisboa.tecnico.socialsoftware.tutor.notifications;

public interface Observable {
    void Attach(Observer o);
    void Dettach(Observer o);
    void Notify();
}

// torneio novo - todos alunos
// torneio em que estás inscrito (cancelar e apagar)

// professor faz review a tua submissao

// quizzes (começar)

// discussio - reply de professor / aluno

// anuncios
