<?php

declare(strict_types=1);

function email_layout(string $title, string $bodyHtml): string
{
    $siteName = htmlspecialchars((string) config('site_name'), ENT_QUOTES, 'UTF-8');
    return '<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>'
        . '<body style="margin:0;background:#f6f4ee;color:#26332b;font-family:Arial,sans-serif">'
        . '<div style="max-width:620px;margin:0 auto;padding:32px 18px">'
        . '<div style="background:#fff;border:1px solid #dcded7;border-radius:18px;padding:28px">'
        . '<p style="margin:0 0 18px;color:#5f6f63;font-size:14px">' . $siteName . '</p>'
        . '<h1 style="font-size:24px;line-height:1.25;margin:0 0 18px">' . htmlspecialchars($title, ENT_QUOTES, 'UTF-8') . '</h1>'
        . $bodyHtml
        . '</div></div></body></html>';
}

function button_html(string $label, string $url): string
{
    return '<p style="margin:24px 0"><a href="' . htmlspecialchars($url, ENT_QUOTES, 'UTF-8') . '" style="display:inline-block;background:#315f45;color:#fff;text-decoration:none;padding:12px 18px;border-radius:12px;font-weight:bold">'
        . htmlspecialchars($label, ENT_QUOTES, 'UTF-8') . '</a></p>';
}
