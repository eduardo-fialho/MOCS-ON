function calendarComponent() {
  return {
    // abas
    aba: 'hoje',

    // data
    month: new Date().getMonth(),
    year: new Date().getFullYear(),
    monthName: '',
    blanks: [],
    days: [],

    // tooltip
    tooltipVisible: false,
    tooltipHtml: '',
    tooltipX: 0,
    tooltipY: 0,

    // modal
    modalVisible: false,
    modalDateFormatted: '',
    modalEvents: [],

    // hoje
    hojeEventos: [],

    // MOCK de eventos para testar (substitua por fetch)
    eventosMock: {
      "2025-11-02": [
        { titulo: "Reunião de Diretoria", horario: "09:00 - 10:30", local: "Sala A", descricao: "Discussão metas." },
        { titulo: "Planejamento de Comitê", horario: "14:00 - 16:00", local: "Sala B", descricao: "Agenda semanal." }
      ],
      "2025-11-04": [
        { titulo: "Sessão de Debate", horario: "08:00 - 12:00", local: "Plenária", descricao: "Debate temático." }
      ]
    },

    init() {
      this.updateCalendar();
      this.setHojeEventos();
    },

    updateCalendar() {
      const first = new Date(this.year, this.month, 1);
      const totalDays = new Date(this.year, this.month + 1, 0).getDate();
      const firstDayIndex = first.getDay(); // 0=Dom .. 6=Sáb

      this.blanks = Array(firstDayIndex).fill(null);

      const today = new Date();
      const monthNames = ["Janeiro","Fevereiro","Março","Abril","Maio","Junho",
                          "Julho","Agosto","Setembro","Outubro","Novembro","Dezembro"];
      this.monthName = monthNames[this.month];

      const daysArr = [];
      for (let i = 1; i <= totalDays; i++) {
        const iso = `${this.year}-${String(this.month+1).padStart(2,'0')}-${String(i).padStart(2,'0')}`;
        daysArr.push({
          number: i,
          date: iso,
          isToday: (i === today.getDate() && this.month === today.getMonth() && this.year === today.getFullYear()),
          events: this.eventosMock[iso] ? this.eventosMock[iso] : []
        });
      }
      this.days = daysArr;
    },

    nextMonth() {
      if (this.month === 11) { this.month = 0; this.year++; } else { this.month++; }
      this.updateCalendar();
    },

    prevMonth() {
      if (this.month === 0) { this.month = 11; this.year--; } else { this.month--; }
      this.updateCalendar();
    },

    // TOOLTIP
    showTooltip(e, day) {
      if (!day.events || day.events.length === 0) return;
      this.tooltipHtml = day.events.map(ev => `<div class="mb-1"><strong>${ev.titulo}</strong><div class="text-xs text-gray-500">${ev.horario}</div></div>`).join('');
      const padding = 12;
      const vw = window.innerWidth;
      let left = e.clientX + 10;
      if (left + 260 > vw) left = vw - 270;
      let top = e.clientY - 10;
      if (top < 60) top = e.clientY + 20;
      this.tooltipX = left;
      this.tooltipY = top;
      this.tooltipVisible = true;
    },

    hideTooltip() {
      this.tooltipVisible = false;
    },

    // MODAL
    openModal(day) {
      this.modalEvents = day.events ? day.events : [];
      const d = new Date(day.date);
      this.modalDateFormatted = d.toLocaleDateString('pt-BR', { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' });
      this.modalVisible = true;
    },

    closeModal() {
      this.modalVisible = false;
    },

    // ABA HOJE
    setHojeEventos() {
      const today = new Date();
      const iso = `${today.getFullYear()}-${String(today.getMonth()+1).padStart(2,'0')}-${String(today.getDate()).padStart(2,'0')}`;
      this.hojeEventos = this.eventosMock[iso] ? this.eventosMock[iso] : [];
    }, 

    // exemplo: carregar eventos do backend para o mês atual (substituir)
    carregarEventosDoBackend(ano, mes) {
      // fetch(`/agenda?ano=${ano}&mes=${mes+1}`)
      //   .then(r => r.json())
      //   .then(data => {
      //     // transformar data em map yyyy-mm-dd => [eventos]
      //     // this.eventosMock = {...this.eventosMock, ...novoMapa};
      //     // this.updateCalendar();
      //   });
    }
    
  };
}
