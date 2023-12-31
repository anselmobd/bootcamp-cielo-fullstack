import {
  IFeedBack,
  TipoFeedback,
} from '../../shared/models/tipo-feedback.model';
import { FeedBackService } from '../../shared/services/feedback.service';
import { Component } from '@angular/core';

@Component({
  selector: 'app-adicionar-feedback',
  templateUrl: './adicionar-feedback.component.html',
  styleUrls: ['./adicionar-feedback.component.scss'],
})
export class AdicionarFeedBackComponent {
  tiposDeFeedback: TipoFeedback[] = [
    new TipoFeedback('SUGESTAO', 'Sugestão'),
    new TipoFeedback('CRITICA', 'Crítica'),
    new TipoFeedback('ELOGIO', 'Elogio'),
  ];
  mensagem: string = '';
  tipo: string = '';

  constructor(private feedBackService: FeedBackService) {}

  enviarNovoFeedBack() {
    if (this.mensagem && this.tipo) {
      var novoFeedback: IFeedBack = {
        message: this.mensagem,
        type: this.tipo,
      };

      this.fetchEnviarFeedBack(novoFeedback);
    }
  }

  private fetchEnviarFeedBack(novoFeedback: IFeedBack) {
    this.feedBackService.enviarFeedback(novoFeedback).subscribe(
      (response) => {
        this.mensagem = '';
        this.tipo = '';
        window.location.reload();
      },
      (error) => {
        console.error('Ocorreu um erro ao enviar o feedback:', error);
      }
    );
  }
}
