async function carregarEventosDoBackend(ano, mes) {
    const response = await fetch(`/agenda/eventos?ano=${ano}&mes=${mes + 1}`);
    return await response.json();
}

function calendarComponent() {
    return {
        aba: 'hoje',

        month: new Date().getMonth(),
        year: new Date().getFullYear(),
        monthName: '',
        blanks: [],
        days: [],

        tooltipVisible: false,
        tooltipHtml: '',
        tooltipX: 0,
        tooltipY: 0,

        modalVisible: false,
        modalDateFormatted: '',
        modalEvents: [],

        hojeEventos: [],
        eventos: {},

        async init() {
            await this.carregarEventosMes();
            this.updateCalendar();
            this.loadTodayEvents();
        },

        async carregarEventosMes() {
            const eventosBackend = await carregarEventosDoBackend(this.year, this.month);
            this.eventos = {};

            eventosBackend.forEach(ev => {
                const data = ev.data_evento;

                if (!this.eventos[data]) {
                    this.eventos[data] = [];
                }

                this.eventos[data].push({
                    titulo: ev.titulo,
                    horario: ev.hora_evento,
                    descricao: ev.descricao,
                    local: ev.local || ''
                });
            });
        },

        updateCalendar() {
            const firstDay = new Date(this.year, this.month, 1).getDay();
            const daysInMonth = new Date(this.year, this.month + 1, 0).getDate();

            this.monthName = new Date(this.year, this.month)
                .toLocaleString("pt-BR", { month: "long" })
                .replace(/^\w/, c => c.toUpperCase());

            this.blanks = [...Array(firstDay).keys()];
            this.days = [];

            for (let i = 1; i <= daysInMonth; i++) {
                const date = `${this.year}-${String(this.month + 1).padStart(2, "0")}-${String(i).padStart(2, "0")}`;
                this.days.push({
                    number: i,
                    date: date,
                    isToday: this.isToday(i),
                    events: this.eventos[date] || []
                });
            }
        },

        async prevMonth() {
            if (this.month === 0) {
                this.month = 11;
                this.year--;
            } else {
                this.month--;
            }
            await this.carregarEventosMes();
            this.updateCalendar();
        },

        async nextMonth() {
            if (this.month === 11) {
                this.month = 0;
                this.year++;
            } else {
                this.month++;
            }
            await this.carregarEventosMes();
            this.updateCalendar();
        },

        isToday(day) {
            const hoje = new Date();
            return (
                day === hoje.getDate() &&
                this.month === hoje.getMonth() &&
                this.year === hoje.getFullYear()
            );
        },

        loadTodayEvents() {
            const hoje = new Date();
            const hojeStr = `${hoje.getFullYear()}-${String(
                hoje.getMonth() + 1
            ).padStart(2, "0")}-${String(hoje.getDate()).padStart(2, "0")}`;

            this.hojeEventos = this.eventos[hojeStr] || [];
        },

        showTooltip(event, day) {
            if (!day.events.length) return;

            this.tooltipHtml = day.events
                .map(ev => `<div><b>${ev.titulo}</b><br>${ev.horario}</div>`)
                .join("<hr>");

            this.tooltipX = event.target.offsetLeft + 20;
            this.tooltipY = event.target.offsetTop + 40;
            this.tooltipVisible = true;
        },

        hideTooltip() {
            this.tooltipVisible = false;
        },

        openModal(day) {
            this.modalDateFormatted = `${day.number} de ${this.monthName} de ${this.year}`;
            this.modalEvents = day.events;
            this.modalVisible = true;
        },

        closeModal() {
            this.modalVisible = false;
        }
    };
}