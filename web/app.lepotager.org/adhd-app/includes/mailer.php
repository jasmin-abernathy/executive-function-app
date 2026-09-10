<?php

declare(strict_types=1);

final class SimpleSmtpMailer
{
    private $socket;

    public function send(string $to, string $subject, string $html, string $text): void
    {
        $cfg = config('mail.smtp');
        $host = (string) $cfg['host'];
        $port = (int) $cfg['port'];
        $encryption = (string) $cfg['encryption'];
        $transportHost = $encryption === 'ssl' ? 'ssl://' . $host : $host;

        $this->socket = @stream_socket_client(
            $transportHost . ':' . $port,
            $errno,
            $errstr,
            20,
            STREAM_CLIENT_CONNECT
        );

        if (!is_resource($this->socket)) {
            throw new RuntimeException("SMTP connection failed: {$errstr} ({$errno})");
        }

        stream_set_timeout($this->socket, 20);
        $this->expect([220]);
        $this->command('EHLO ' . ($_SERVER['SERVER_NAME'] ?? 'localhost'), [250]);

        if ($encryption === 'tls') {
            $this->command('STARTTLS', [220]);
            if (!stream_socket_enable_crypto($this->socket, true, STREAM_CRYPTO_METHOD_TLS_CLIENT)) {
                throw new RuntimeException('Unable to enable SMTP TLS.');
            }
            $this->command('EHLO ' . ($_SERVER['SERVER_NAME'] ?? 'localhost'), [250]);
        }

        $username = (string) ($cfg['username'] ?? '');
        $password = (string) ($cfg['password'] ?? '');
        if ($username !== '') {
            $this->command('AUTH LOGIN', [334]);
            $this->command(base64_encode($username), [334]);
            $this->command(base64_encode($password), [235]);
        }

        $fromEmail = (string) config('mail.from_email');
        $fromName = (string) config('mail.from_name');
        $replyTo = (string) config('mail.reply_to', $fromEmail);
        $boundary = '=_Potager_' . bin2hex(random_bytes(12));
        $encodedSubject = '=?UTF-8?B?' . base64_encode($subject) . '?=';
        $encodedFrom = '=?UTF-8?B?' . base64_encode($fromName) . '?=';

        $headers = [
            'Date: ' . date(DATE_RFC2822),
            'From: ' . $encodedFrom . ' <' . $fromEmail . '>',
            'Reply-To: ' . $replyTo,
            'To: ' . $to,
            'Subject: ' . $encodedSubject,
            'MIME-Version: 1.0',
            'Content-Type: multipart/alternative; boundary="' . $boundary . '"',
        ];

        $body = implode("\r\n", $headers) . "\r\n\r\n";
        $body .= '--' . $boundary . "\r\n";
        $body .= "Content-Type: text/plain; charset=UTF-8\r\n";
        $body .= "Content-Transfer-Encoding: quoted-printable\r\n\r\n";
        $body .= quoted_printable_encode($text) . "\r\n";
        $body .= '--' . $boundary . "\r\n";
        $body .= "Content-Type: text/html; charset=UTF-8\r\n";
        $body .= "Content-Transfer-Encoding: quoted-printable\r\n\r\n";
        $body .= quoted_printable_encode($html) . "\r\n";
        $body .= '--' . $boundary . "--\r\n";

        $this->command('MAIL FROM:<' . $fromEmail . '>', [250]);
        $this->command('RCPT TO:<' . $to . '>', [250, 251]);
        $this->command('DATA', [354]);
        $safeBody = preg_replace('/(?m)^\./', '..', $body) ?? $body;
        fwrite($this->socket, $safeBody . "\r\n.\r\n");
        $this->expect([250]);
        $this->command('QUIT', [221]);
        fclose($this->socket);
    }

    private function command(string $command, array $codes): string
    {
        fwrite($this->socket, $command . "\r\n");
        return $this->expect($codes);
    }

    private function expect(array $codes): string
    {
        $response = '';
        while (($line = fgets($this->socket, 515)) !== false) {
            $response .= $line;
            if (preg_match('/^(\d{3})[ -]/', $line, $match) && isset($line[3]) && $line[3] === ' ') {
                $code = (int) $match[1];
                if (!in_array($code, $codes, true)) {
                    throw new RuntimeException('Unexpected SMTP response: ' . trim($response));
                }
                return $response;
            }
        }
        throw new RuntimeException('SMTP server closed the connection unexpectedly.');
    }
}

function send_app_email(string $to, string $subject, string $html, string $text): void
{
    if (!valid_email($to)) {
        throw new InvalidArgumentException('Invalid recipient email.');
    }

    if (config('mail.mode') === 'smtp') {
        (new SimpleSmtpMailer())->send($to, $subject, $html, $text);
        return;
    }

    $fromEmail = (string) config('mail.from_email');
    $fromName = (string) config('mail.from_name');
    $replyTo = (string) config('mail.reply_to', $fromEmail);
    $boundary = '=_Potager_' . bin2hex(random_bytes(12));
    $headers = [
        'MIME-Version: 1.0',
        'Content-Type: multipart/alternative; boundary="' . $boundary . '"',
        'From: ' . mb_encode_mimeheader($fromName, 'UTF-8') . ' <' . $fromEmail . '>',
        'Reply-To: ' . $replyTo,
    ];
    $body = '--' . $boundary . "\r\n";
    $body .= "Content-Type: text/plain; charset=UTF-8\r\n\r\n" . $text . "\r\n";
    $body .= '--' . $boundary . "\r\n";
    $body .= "Content-Type: text/html; charset=UTF-8\r\n\r\n" . $html . "\r\n";
    $body .= '--' . $boundary . '--';

    if (!mail($to, mb_encode_mimeheader($subject, 'UTF-8'), $body, implode("\r\n", $headers))) {
        throw new RuntimeException('PHP mail() could not send the message.');
    }
}
