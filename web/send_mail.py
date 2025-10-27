#!/usr/bin/env python3
import argparse
import json
import os
import smtplib
import ssl
from email.message import EmailMessage


def load_config(path: str | None) -> dict:
    if path:
        with open(path, 'r', encoding='utf-8') as f:
            return json.load(f)
    # Sem arquivo: lê de variáveis de ambiente
    def env_bool(name: str, default: bool | None = None):
        val = os.getenv(name)
        if val is None:
            return default
        return val.strip().lower() in ("1", "true", "yes", "on")

    cfg = {
        'host': os.getenv('SMTP_HOST'),
        'port': int(os.getenv('SMTP_PORT', '587')),
        'username': os.getenv('SMTP_USERNAME'),
        'password': os.getenv('SMTP_PASSWORD'),
        'from_email': os.getenv('SMTP_FROM_EMAIL'),
        'from_name': os.getenv('SMTP_FROM_NAME', ''),
        'use_starttls': env_bool('SMTP_USE_STARTTLS', True),
        'use_ssl': env_bool('SMTP_USE_SSL', False),
    }
    if not cfg['from_email']:
        # fallback: usa o próprio usuário como remetente
        cfg['from_email'] = cfg['username']
    return cfg


def build_message(from_email: str, from_name: str, to_email: str, subject: str, body_text: str) -> EmailMessage:
    msg = EmailMessage()
    msg['From'] = f"{from_name} <{from_email}>" if from_name else from_email
    msg['To'] = to_email
    msg['Subject'] = subject
    msg.set_content(body_text)
    return msg


def send_via_smtp(cfg: dict, msg: EmailMessage):
    host = cfg.get('host')
    port = int(cfg.get('port', 587))
    username = cfg.get('username')
    password = cfg.get('password')
    use_ssl = bool(cfg.get('use_ssl', False))
    use_starttls = bool(cfg.get('use_starttls', True))

    if use_ssl:
        context = ssl.create_default_context()
        with smtplib.SMTP_SSL(host, port, context=context) as server:
            if username and password:
                server.login(username, password)
            server.send_message(msg)
    else:
        with smtplib.SMTP(host, port) as server:
            server.ehlo()
            if use_starttls:
                context = ssl.create_default_context()
                server.starttls(context=context)
                server.ehlo()
            if username and password:
                server.login(username, password)
            server.send_message(msg)


def main():
    parser = argparse.ArgumentParser(description='Enviar e-mails via SMTP (Python, sem MTA).')
    parser.add_argument('--config', required=False, help='Opcional: caminho do smtp_config.json. Se ausente, usa variáveis de ambiente SMTP_*')
    parser.add_argument('--to', required=True, help='Destinatário')
    parser.add_argument('--subject', required=True, help='Assunto')
    parser.add_argument('--text-file', required=True, help='Arquivo de texto com o corpo')
    args = parser.parse_args()

    cfg = load_config(args.config)
    from_email = cfg.get('from_email')
    from_name = cfg.get('from_name', '')
    host = cfg.get('host')
    if not host:
        raise SystemExit('Config ausente: defina SMTP_HOST (e demais) em variáveis de ambiente ou informe --config.')
    if not from_email:
        raise SystemExit('Defina SMTP_FROM_EMAIL (ou from_email em --config).')

    if not os.path.isfile(args.text_file):
        raise SystemExit('Arquivo de corpo não encontrado: ' + args.text_file)

    with open(args.text_file, 'r', encoding='utf-8') as f:
        body = f.read()

    msg = build_message(from_email, from_name, args.to, args.subject, body)
    send_via_smtp(cfg, msg)


if __name__ == '__main__':
    main()
